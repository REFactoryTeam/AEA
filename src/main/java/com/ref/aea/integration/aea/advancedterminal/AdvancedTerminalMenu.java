package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageHelper;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.util.ConfigInventory;
import appeng.util.inv.PlayerInternalInventory;
import com.google.common.base.Preconditions;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftConfirmMenu;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AdvancedTerminalMenu extends MEStorageMenu implements IMenuCraftingPacket {

  public static final MenuType<AdvancedTerminalMenu> TYPE =
      MenuTypeBuilder.create(AdvancedTerminalMenu::new, AdvancedTerminalPart.class)
          .build("advanced_terminal");

  private static final String ACTION_CLEAR_TO_PLAYER = "clearToPlayer";
  private static final String ACTION_SET_MODE = "setMode";
  private static final String ACTION_SET_SUBST = "setSubst";
  private static final String ACTION_SET_FLUID = "setFluid";
  private static final String ACTION_CLEAR_PROCESSING = "clearProcessing";

  private final AdvancedTerminalPart part;
  private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
  private final AppEngSlot[] processingSlots = new AppEngSlot[81];
  private final AdvancedCraftingTermSlot outputSlot;

  private Recipe<CraftingContainer> currentRecipe;
  private final TransientCraftingContainer recipeTestContainer =
      new TransientCraftingContainer(this, 3, 3);

  @GuiSync(627)
  public AdvancedTerminalMode mode;

  @GuiSync(628)
  public boolean substitutions;

  @GuiSync(629)
  public boolean fluidSubstitutions;

  @GuiSync(630)
  public int waitingCraftingMask = 0;

  @GuiSync(631)
  public boolean isProcessingWaiting = false;

  public IntSet slotsSupportingFluidSubstitution = new IntArraySet();
  private final Runnable gridListener = () -> updateCurrentRecipeAndOutput(false);

  public AdvancedTerminalMenu(int id, Inventory ip, AdvancedTerminalPart part) {
    super(TYPE, id, ip, part, true);
    this.part = part;

    for (int i = 0; i < 9; i++) {
      this.addSlot(
          this.craftingSlots[i] = new CraftingMatrixSlot(this, part.getCraftingGrid(), i),
          SlotSemantics.CRAFTING_GRID);
    }

    this.addSlot(
        this.outputSlot =
            new AdvancedCraftingTermSlot(
                this.getPlayerInventory().player,
                this.getActionSource(),
                this.powerSource,
                part.getInventory(),
                part.getCraftingGrid(),
                part.getCraftingGrid(),
                this,
                part),
        SlotSemantics.CRAFTING_RESULT);

    for (int i = 0; i < 81; i++) {
      this.addSlot(
          this.processingSlots[i] = new AppEngSlot(part.getProcessingGrid().createMenuWrapper(), i),
          SlotSemantics.PROCESSING_INPUTS);
    }

    part.addGridListener(this.gridListener);

    registerClientAction(ACTION_CLEAR_TO_PLAYER, this::clearToPlayerInventory);
    registerClientAction(ACTION_SET_MODE, AdvancedTerminalMode.class, this::setMode);
    registerClientAction(ACTION_SET_SUBST, Boolean.class, this::setSubstitutions);
    registerClientAction(ACTION_SET_FLUID, Boolean.class, this::setFluidSubstitutions);
    registerClientAction(ACTION_CLEAR_PROCESSING, this::clearProcessingGridServer);

    updateCurrentRecipeAndOutput(true);
  }

  @Override
  public void removed(@NotNull Player player) {
    super.removed(player);
    part.removeGridListener(this.gridListener);
  }

  @Override
  public void slotsChanged(@NotNull Container inventory) {
    if (inventory != recipeTestContainer) {
      updateCurrentRecipeAndOutput(false);
    }
  }

  public Recipe<CraftingContainer> getCurrentRecipe() {
    return this.currentRecipe;
  }

  private void updateCurrentRecipeAndOutput(boolean forceUpdate) {
    boolean hasChanged = forceUpdate;
    for (int x = 0; x < 9; x++) {
      ItemStack stack = this.craftingSlots[x].getItem();
      if (!ItemStack.isSameItemSameTags(stack, recipeTestContainer.getItem(x))) {
        hasChanged = true;
        recipeTestContainer.setItem(x, stack.copy());
      }
    }

    if (!hasChanged) return;

    Level level = this.getPlayerInventory().player.level();
    this.currentRecipe =
        level
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, recipeTestContainer, level)
            .orElse(null);

    if (this.currentRecipe == null) {
      this.outputSlot.set(ItemStack.EMPTY);
    } else {
      this.outputSlot.set(this.currentRecipe.assemble(recipeTestContainer, level.registryAccess()));
    }
    checkFluidSubstitutionSupport();
  }

  private void checkFluidSubstitutionSupport() {
    this.slotsSupportingFluidSubstitution.clear();
    if (this.currentRecipe == null) return;

    for (int i = 0; i < 9; i++) {
      ItemStack stack = recipeTestContainer.getItem(i);
      if (!stack.isEmpty()) {
        var containedFluid = ContainerItemStrategies.getContainedStack(stack, AEKeyType.fluids());
        boolean isBucket =
            stack.getItem() instanceof net.minecraft.world.item.BucketItem
                || stack.getItem() instanceof net.minecraft.world.item.MilkBucketItem;
        if (containedFluid != null && isBucket) {
          slotsSupportingFluidSubstitution.add(i);
        }
      }
    }
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if (isServerSide()) {
      this.mode = part.getMode();
      this.substitutions = part.isSubstitutions();
      this.fluidSubstitutions = part.isFluidSubstitutions();
      updateActiveCraftingMasks();
      updateSlotVisibility();
    }
  }

  private void updateActiveCraftingMasks() {
    int newCraftingMask = 0;
    boolean processingActive = false;

    for (var entry : part.getActiveLinks().values()) {
      if (entry.mode() == AdvancedTerminalMode.CRAFTING) {
        for (int s : entry.slots()) {
          if (s >= 0 && s < 9) {
            newCraftingMask |= (1 << s);
          }
        }
      } else {
        processingActive = true;
      }
    }

    this.waitingCraftingMask = newCraftingMask;
    this.isProcessingWaiting = processingActive;
  }

  private void updateSlotVisibility() {
    boolean isCrafting = mode == AdvancedTerminalMode.CRAFTING;
    this.outputSlot.setActive(isCrafting);
    for (CraftingMatrixSlot slot : craftingSlots) slot.setActive(isCrafting);
    for (AppEngSlot slot : processingSlots) slot.setActive(!isCrafting);
  }

  public void clearGrid() {
    for (AdvancedTerminalMode m : AdvancedTerminalMode.values()) clearGridForMode(m);
  }

  public void clearGridForMode(AdvancedTerminalMode m) {
    if (m == AdvancedTerminalMode.CRAFTING) {
      Preconditions.checkState(isClientSide());
      NetworkHandler.instance()
          .sendToServer(
              new InventoryActionPacket(InventoryAction.MOVE_REGION, craftingSlots[0].index, 0));
    } else if (m == AdvancedTerminalMode.PROCESSING && isClientSide()) {
      sendClientAction(ACTION_CLEAR_PROCESSING);
    }
  }

  private void clearProcessingGridServer() {
    var gridNode = part.getGridNode();
    if (gridNode == null || gridNode.getGrid() == null) return;
    var grid = gridNode.getGrid();
    var storage = grid.getStorageService().getInventory();
    var energy = grid.getEnergyService();
    var actionSrc = getActionSource();
    ConfigInventory processingGrid = part.getProcessingGrid();

    for (int i = 0; i < processingGrid.size(); i++) {
      AEKey key = processingGrid.getKey(i);
      long amount = processingGrid.getAmount(i);
      if (key != null && amount > 0) {
        long inserted = StorageHelper.poweredInsert(energy, storage, key, amount, actionSrc);
        if (inserted > 0) {
          processingGrid.extract(i, key, inserted, Actionable.MODULATE);
        }
      }
    }
  }

  public void clearToPlayerInventory() {
    if (isClientSide()) {
      sendClientAction(ACTION_CLEAR_TO_PLAYER);
      return;
    }
    PlayerInternalInventory playerInv = new PlayerInternalInventory(getPlayerInventory());

    if (mode == AdvancedTerminalMode.CRAFTING) {
      InternalInventory activeGrid = part.getCraftingGrid();
      for (int i = 0; i < activeGrid.size(); ++i) {
        ItemStack gridStack = activeGrid.getStackInSlot(i);
        if (gridStack.isEmpty()) continue;

        ItemStack toTransfer = gridStack.copy();
        for (int emptyLoop = 0; emptyLoop < 2; ++emptyLoop) {
          boolean allowEmpty = (emptyLoop == 1);
          toTransfer = getItemStackForPlayer(playerInv, toTransfer, allowEmpty);
        }

        int transferred = gridStack.getCount() - toTransfer.getCount();
        if (transferred > 0) {
          activeGrid.extractItem(i, transferred, false);
        }
      }
    } else {
      ConfigInventory activeGrid = part.getProcessingGrid();
      for (int i = 0; i < activeGrid.size(); ++i) {
        AEKey key = activeGrid.getKey(i);
        long amount = activeGrid.getAmount(i);

        if (key instanceof AEItemKey itemKey && amount > 0) {
          ItemStack stack = itemKey.toStack((int) amount);
          for (int emptyLoop = 0; emptyLoop < 2; ++emptyLoop) {
            boolean allowEmpty = emptyLoop == 1;
            stack = getItemStackForPlayer(playerInv, stack, allowEmpty);
          }

          long accepted = amount - stack.getCount();
          if (accepted > 0) {
            activeGrid.extract(i, itemKey, accepted, Actionable.MODULATE);
          }
        }
      }
    }
  }

  private ItemStack getItemStackForPlayer(
      PlayerInternalInventory playerInv, ItemStack stack, boolean allowEmpty) {
    for (int j = 9; j-- > 0; )
      if (playerInv.getStackInSlot(j).isEmpty() == allowEmpty)
        stack = playerInv.getSlotInv(j).addItems(stack);
    for (int j = 9; j < Inventory.INVENTORY_SIZE; ++j)
      if (playerInv.getStackInSlot(j).isEmpty() == allowEmpty)
        stack = playerInv.getSlotInv(j).addItems(stack);
    return stack;
  }

  public void startAdvancedAutoCrafting(
      List<AdvancedAutoCraftEntry> stacksToCraft, AdvancedTerminalMode mode) {
    AdvancedCraftConfirmMenu.openWithCraftingList(
        getActionHost(), (ServerPlayer) getPlayer(), getLocator(), stacksToCraft, mode);
  }

  @Override
  public void startAutoCrafting(List<AutoCraftEntry> toCraft) {}

  @Override
  public InternalInventory getCraftingMatrix() {
    return part.getCraftingGrid();
  }

  public ConfigInventory getProcessingGrid() {
    return part.getProcessingGrid();
  }

  @Override
  public boolean useRealItems() {
    return true;
  }

  public AdvancedCraftingTermSlot getOutputSlot() {
    return outputSlot;
  }

  public AppEngSlot[] getCraftingSlots() {
    return craftingSlots;
  }

  public AppEngSlot[] getProcessingSlots() {
    return processingSlots;
  }

  public void setMode(AdvancedTerminalMode advancedTerminalMode) {
    if (isClientSide()) {
      sendClientAction(ACTION_SET_MODE, advancedTerminalMode);
      return;
    }
    part.setMode(advancedTerminalMode);
    for (var slot : getProcessingSlots())
      slot.setSlotEnabled(advancedTerminalMode == AdvancedTerminalMode.PROCESSING);
  }

  public void setSubstitutions(boolean b) {
    if (isClientSide()) sendClientAction(ACTION_SET_SUBST, b);
    else part.setSubstitutions(b);
  }

  public void setFluidSubstitutions(boolean b) {
    if (isClientSide()) sendClientAction(ACTION_SET_FLUID, b);
    else part.setFluidSubstitutions(b);
  }

  public boolean isSlotWaiting(int slotIndex, AdvancedTerminalMode currentMode) {
    if (currentMode == AdvancedTerminalMode.CRAFTING) {
      return (waitingCraftingMask & (1 << slotIndex)) != 0;
    } else {
      return isProcessingWaiting;
    }
  }
}

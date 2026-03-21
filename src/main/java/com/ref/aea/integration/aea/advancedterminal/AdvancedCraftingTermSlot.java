package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.CraftingEvent;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.menu.slot.CraftingTermSlot;
import appeng.util.Platform;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

/**
 * Slot implementation for the advanced crafting terminal output. Handles the extraction of
 * ingredients from the ME network and standard crafting mechanics.
 */
public class AdvancedCraftingTermSlot extends CraftingTermSlot {

  private final InternalInventory pattern;
  private final InternalInventory craftInv;
  private final IActionSource mySrc;
  private final IEnergySource energySrc;
  private final MEStorage storage;
  private final IMenuCraftingPacket menu;
  private final AdvancedTerminalPart part;

  public AdvancedCraftingTermSlot(
      Player player,
      IActionSource mySrc,
      IEnergySource energySrc,
      MEStorage storage,
      InternalInventory cMatrix,
      InternalInventory secondMatrix,
      IMenuCraftingPacket ccp,
      AdvancedTerminalPart part) {
    super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, ccp);
    this.mySrc = mySrc;
    this.energySrc = energySrc;
    this.storage = storage;
    this.pattern = cMatrix;
    this.craftInv = secondMatrix;
    this.menu = ccp;
    this.part = part;
  }

  @Override
  public boolean mayPickup(Player player) {
    return false;
  }

  @Override
  public void doClick(InventoryAction action, Player who) {
    ItemStack result = this.getItem();
    if (result.isEmpty() || isRemote()) return;

    int howManyPerCraft = result.getCount();
    int maxTimesToCraft;
    InternalInventory targetInv;

    if (action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL) {
      targetInv = new PlayerInternalInventory(who.getInventory());
      maxTimesToCraft =
          action == InventoryAction.CRAFT_SHIFT
              ? (result.getMaxStackSize() / howManyPerCraft)
              : (result.getMaxStackSize() / howManyPerCraft * 36);
    } else if (action == InventoryAction.CRAFT_STACK) {
      targetInv = new CarriedItemInventory(getMenu());
      maxTimesToCraft = result.getMaxStackSize() / howManyPerCraft;
    } else {
      if (getMenu().getCarried().isEmpty()) {
        getMenu().setCarried(performCraft(who));
        return;
      }
      targetInv = new CarriedItemInventory(getMenu());
      maxTimesToCraft = 1;
    }

    ItemStack itemAtStart = result.copy();
    for (int i = 0; i < maxTimesToCraft; i++) {
      if (!ItemStack.isSameItemSameTags(itemAtStart, getItem())) break;
      if (!targetInv.simulateAdd(itemAtStart).isEmpty()) break;

      ItemStack crafted = performCraft(who);
      if (crafted.isEmpty()) break;

      ItemStack remainder = targetInv.addItems(crafted);
      if (!remainder.isEmpty()) {
        Platform.spawnDrops(who.level(), who.blockPosition(), List.of(remainder));
        break;
      }
    }
  }

  private ItemStack performCraft(Player player) {
    Level level = player.level();
    ItemStack resultStack = this.getItem().copy();
    if (resultStack.isEmpty()) return ItemStack.EMPTY;

    TransientCraftingContainer ic = new TransientCraftingContainer(player.containerMenu, 3, 3);
    for (int x = 0; x < 9; x++) {
      ic.setItem(x, pattern.getStackInSlot(x).copy());
    }

    Recipe<CraftingContainer> recipe = findRecipe(ic, level);
    if (recipe == null) {
      return attemptRepairCrafting(player, level, resultStack, ic);
    }

    resultStack = recipe.assemble(ic, level.registryAccess());
    ItemStack[] restockSet = extractTemplateItems(recipe, ic, resultStack, level);

    ForgeHooks.setCraftingPlayer(player);
    NonNullList<ItemStack> remainders = recipe.getRemainingItems(ic);
    ForgeHooks.setCraftingPlayer(null);

    CraftingEvent.fireCraftingEvent(player, resultStack, this.craftInv.toContainer());
    resultStack.onCraftedBy(level, player, resultStack.getCount());

    for (int x = 0; x < 9; x++) {
      if (!pattern.getStackInSlot(x).isEmpty()) {
        pattern.extractItem(x, 1, false);
      }
    }

    handleRemaindersAndRestock(player, ic, remainders, restockSet);

    player.containerMenu.slotsChanged(this.craftInv.toContainer());
    return resultStack;
  }

  private ItemStack attemptRepairCrafting(
      Player player, Level level, ItemStack resultStack, TransientCraftingContainer ic) {
    final Item target = resultStack.getItem();
    if (target.canBeDepleted() && target.isValidRepairItem(resultStack, resultStack)) {
      boolean isValidRepair = true;
      for (int x = 0; x < ic.getContainerSize(); x++) {
        final ItemStack pis = ic.getItem(x);
        if (!pis.isEmpty() && pis.getItem() != target) {
          isValidRepair = false;
          break;
        }
      }
      if (isValidRepair) {
        CraftingEvent.fireCraftingEvent(player, resultStack, this.craftInv.toContainer());
        resultStack.onCraftedBy(level, player, resultStack.getCount());
        for (int x = 0; x < 9; x++) {
          if (!pattern.getStackInSlot(x).isEmpty()) {
            pattern.extractItem(x, 1, false);
          }
        }
        player.containerMenu.slotsChanged(this.craftInv.toContainer());
        return resultStack;
      }
    }
    return ItemStack.EMPTY;
  }

  private ItemStack[] extractTemplateItems(
      Recipe<CraftingContainer> recipe,
      TransientCraftingContainer ic,
      ItemStack resultStack,
      Level level) {
    ItemStack[] restockSet = new ItemStack[9];
    KeyCounter availableStacks = storage.getAvailableStacks();
    var filter = ViewCellItem.createItemFilter(this.menu.getViewCells());

    for (int x = 0; x < 9; x++) {
      ItemStack template = pattern.getStackInSlot(x);
      if (template.isEmpty()) continue;

      ItemStack extracted =
          Platform.extractItemsByRecipe(
              this.energySrc,
              this.mySrc,
              this.storage,
              level,
              recipe,
              resultStack,
              ic,
              template,
              x,
              availableStacks,
              Actionable.MODULATE,
              filter);

      if (extracted.isEmpty() && part.isSubstitutions()) {
        extracted = attemptSubstitutionExtraction(recipe, template, availableStacks);
      }
      restockSet[x] = extracted;
    }
    return restockSet;
  }

  private ItemStack attemptSubstitutionExtraction(
      Recipe<CraftingContainer> recipe, ItemStack template, KeyCounter availableStacks) {
    for (Ingredient ing : recipe.getIngredients()) {
      if (ing.test(template)) {
        for (ItemStack validStack : ing.getItems()) {
          AEItemKey key = AEItemKey.of(validStack);
          if (key != null && availableStacks.get(key) > 0) {
            long extractedSub = storage.extract(key, 1, Actionable.MODULATE, mySrc);
            if (extractedSub > 0) {
              return key.toStack(1);
            }
          }
        }
      }
    }
    return ItemStack.EMPTY;
  }

  private void handleRemaindersAndRestock(
      Player player,
      TransientCraftingContainer ic,
      NonNullList<ItemStack> remainders,
      ItemStack[] restockSet) {
    for (int x = 0; x < 9; x++) {
      ItemStack remainder = remainders.get(x);
      ItemStack template = ic.getItem(x);

      if (part.isFluidSubstitutions() && !template.isEmpty() && !remainder.isEmpty()) {
        remainder = processFluidSubstitutionRemainder(template, remainder, restockSet, x);
      }

      if (!remainder.isEmpty()) {
        if (pattern.getStackInSlot(x).isEmpty() && remainder.is(template.getItem())) {
          pattern.setItemDirect(x, remainder);
          remainder = ItemStack.EMPTY;
        }
        returnItemToNetworkOrPlayer(player, remainder);
      }

      ItemStack restock = restockSet[x];
      if (restock != null && !restock.isEmpty()) {
        if (pattern.getStackInSlot(x).isEmpty()) {
          pattern.setItemDirect(x, restock);
        } else {
          returnItemToNetworkOrPlayer(player, restock);
        }
      }
    }
  }

  private ItemStack processFluidSubstitutionRemainder(
      ItemStack template, ItemStack remainder, ItemStack[] restockSet, int index) {
    var containedFluid = ContainerItemStrategies.getContainedStack(template, AEKeyType.fluids());
    boolean isBucket =
        template.getItem() instanceof net.minecraft.world.item.BucketItem
            || template.getItem() instanceof net.minecraft.world.item.MilkBucketItem;

    if (containedFluid != null && isBucket && remainder.is(Items.BUCKET)) {
      appeng.api.stacks.AEKey fluidKey = containedFluid.what();
      long needed = containedFluid.amount();
      long extractedFluid = storage.extract(fluidKey, needed, Actionable.SIMULATE, mySrc);
      if (extractedFluid == needed) {
        storage.extract(fluidKey, needed, Actionable.MODULATE, mySrc);
        restockSet[index] = template.copy();
        restockSet[index].setCount(1);
        return ItemStack.EMPTY;
      }
    }
    return remainder;
  }

  private void returnItemToNetworkOrPlayer(Player player, ItemStack stack) {
    AEItemKey key = AEItemKey.of(stack);
    if (key != null) {
      long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, mySrc);
      if (inserted < stack.getCount()) {
        player
            .getInventory()
            .placeItemBackInInventory(stack.copyWithCount((int) (stack.getCount() - inserted)));
      }
    } else {
      player.getInventory().placeItemBackInInventory(stack);
    }
  }

  @Override
  protected Recipe<CraftingContainer> findRecipe(CraftingContainer ic, Level level) {
    if (this.menu instanceof AdvancedTerminalMenu terminalMenu) {
      var recipe = terminalMenu.getCurrentRecipe();
      if (recipe != null && recipe.matches(ic, level)) {
        return recipe;
      }
    }
    return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
  }
}

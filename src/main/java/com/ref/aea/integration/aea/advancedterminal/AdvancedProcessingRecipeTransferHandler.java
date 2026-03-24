package com.ref.aea.integration.aea.advancedterminal;

import static appeng.integration.modules.jeirei.TransferHelper.*;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jei.JEIPlugin;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.common.GridInventoryEntry;
import com.ref.aea.network.AEANetwork;
import java.util.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedProcessingRecipeTransferHandler
    implements IUniversalRecipeTransferHandler<AdvancedTerminalMenu> {

  private final IRecipeTransferHandlerHelper helper;
  private final IStackHelper stackHelper;
  private static final int MAX_PROCESSING_SLOTS = 81;

  public AdvancedProcessingRecipeTransferHandler(
      IRecipeTransferHandlerHelper helper, IStackHelper stackHelper) {
    this.helper = helper;
    this.stackHelper = stackHelper;
  }

  @Override
  public @NotNull Class<AdvancedTerminalMenu> getContainerClass() {
    return AdvancedTerminalMenu.class;
  }

  @Override
  public @NotNull Optional<MenuType<AdvancedTerminalMenu>> getMenuType() {
    return Optional.of(AdvancedTerminalMenu.TYPE);
  }

  @Override
  public IRecipeTransferError transferRecipe(
      @NotNull AdvancedTerminalMenu menu,
      @NotNull Object recipe,
      @NotNull IRecipeSlotsView display,
      @NotNull Player player,
      boolean maxTransfer,
      boolean doTransfer) {

    List<List<GenericStack>> inputs = GenericEntryStackHelper.ofInputs(display);

    if (inputs.size() > MAX_PROCESSING_SLOTS) {
      return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
    }

    if (menu.mode != AdvancedTerminalMode.PROCESSING) {
      menu.setMode(AdvancedTerminalMode.PROCESSING);
    }

    InventoryLookup lookup = new InventoryLookup();

    populateLookup(menu, player, lookup);

    RecipeMatchResult result = matchRecipeInputs(inputs, lookup);

    if (result.missingIndices().size() == inputs.size() && !inputs.isEmpty()) {
      return helper.createUserErrorForMissingSlots(
          ItemModText.NO_ITEMS.text(), display.getSlotViews(RecipeIngredientRole.INPUT));
    }

    boolean craftMissing = Screen.hasControlDown();
    if (doTransfer) {
      AEANetwork.INSTANCE.sendToServer(
          new AdvancedFillProcessingGridPacket(result.exactRequests(), craftMissing));
      return null;
    }

    if (!result.missingIndices().isEmpty() || !result.craftableIndices().isEmpty()) {
      int color =
          !result.missingIndices().isEmpty() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
      return new ErrorRenderer(
          result.missingIndices(), result.craftableIndices(), craftMissing, color);
    }

    return null;
  }

  private void populateLookup(AdvancedTerminalMenu menu, Player player, InventoryLookup lookup) {
    if (menu.getClientRepo() != null) {
      for (GridInventoryEntry entry : menu.getClientRepo().getAllEntries()) {
        AEKey key = entry.getWhat();
        if (entry.getStoredAmount() > 0) {
          lookup.addAvailable(key, entry.getStoredAmount());
        }
        if (entry.isCraftable()) {
          lookup.addCraftable(key);
        }
      }
    }
    countPlayerInventory(player, lookup.availableCounts, lookup.availableItemsMap);
  }

  public void countPlayerInventory(
      Player player,
      Map<AEKey, Long> availableCounts,
      Map<Item, Set<AEItemKey>> availableItemsMap) {
    for (int i = 0; i < player.getInventory().items.size(); i++) {
      var stack = player.getInventory().getItem(i);
      if (!stack.isEmpty()) {
        AEItemKey key = AEItemKey.of(stack);
        if (key != null) {
          availableCounts.merge(key, (long) stack.getCount(), Long::sum);
          availableItemsMap.computeIfAbsent(key.getItem(), k -> new HashSet<>()).add(key);
        }
      }
    }
  }

  private RecipeMatchResult matchRecipeInputs(
      List<List<GenericStack>> inputs, InventoryLookup lookup) {
    List<GenericStack> exactRequests = new ArrayList<>();
    Set<Integer> missingIndices = new HashSet<>();
    Set<Integer> craftableIndices = new HashSet<>();

    for (int i = 0; i < inputs.size(); i++) {
      List<GenericStack> alternatives = inputs.get(i);
      if (alternatives.isEmpty()) continue;

      GenericStack match = findBestMatch(alternatives, lookup);

      if (match == null) {
        long amount = alternatives.get(0).amount();
        exactRequests.add(new GenericStack(alternatives.get(0).what(), amount));
        missingIndices.add(i);
      } else {
        exactRequests.add(match);
        if (lookup.isLastMatchCraftable) {
          craftableIndices.add(i);
        }
      }
    }
    return new RecipeMatchResult(exactRequests, missingIndices, craftableIndices);
  }

  private GenericStack findBestMatch(List<GenericStack> alternatives, InventoryLookup lookup) {
    lookup.isLastMatchCraftable = false;
    long neededAmount = alternatives.get(0).amount();

    for (GenericStack alt : alternatives) {
      if (lookup.availableCounts.getOrDefault(alt.what(), 0L) >= neededAmount) {
        lookup.consumeAvailable(alt.what(), neededAmount);
        return new GenericStack(alt.what(), neededAmount);
      }
    }

    for (GenericStack alt : alternatives) {
      if (alt.what() instanceof AEItemKey altKey) {
        AEItemKey found =
            findEquivalent(altKey, lookup.availableItemsMap, lookup.availableCounts, neededAmount);
        if (found != null) {
          lookup.consumeAvailable(found, neededAmount);
          return new GenericStack(found, neededAmount);
        }
      }
    }

    for (GenericStack alt : alternatives) {
      if (lookup.craftableKeys.contains(alt.what())) {
        lookup.isLastMatchCraftable = true;
        return new GenericStack(alt.what(), neededAmount);
      }
    }

    for (GenericStack alt : alternatives) {
      if (alt.what() instanceof AEItemKey altKey) {
        AEItemKey found = findEquivalent(altKey, lookup.craftableItemsMap, null, 0);
        if (found != null) {
          lookup.isLastMatchCraftable = true;
          return new GenericStack(found, neededAmount);
        }
      }
    }

    return null;
  }

  public AEItemKey findEquivalent(
      AEItemKey target, Map<Item, Set<AEItemKey>> pool, Map<AEKey, Long> counts, long amount) {
    Set<AEItemKey> candidates = pool.getOrDefault(target.getItem(), Collections.emptySet());
    ItemStack targetStack = target.toStack();
    for (AEItemKey candidate : candidates) {
      if (counts != null && counts.getOrDefault(candidate, 0L) < amount) continue;
      if (stackHelper.isEquivalent(candidate.toStack(), targetStack, UidContext.Ingredient)) {
        return candidate;
      }
    }
    return null;
  }

  public static class InventoryLookup {
    public final Map<AEKey, Long> availableCounts = new HashMap<>();
    public final Set<AEKey> craftableKeys = new HashSet<>();
    public final Map<Item, Set<AEItemKey>> availableItemsMap = new HashMap<>();
    public final Map<Item, Set<AEItemKey>> craftableItemsMap = new HashMap<>();
    public boolean isLastMatchCraftable = false;

    public void addAvailable(AEKey key, long amount) {
      availableCounts.put(key, amount);
      if (key instanceof AEItemKey itemKey) {
        availableItemsMap.computeIfAbsent(itemKey.getItem(), k -> new HashSet<>()).add(itemKey);
      }
    }

    public void addCraftable(AEKey key) {
      craftableKeys.add(key);
      if (key instanceof AEItemKey itemKey) {
        craftableItemsMap.computeIfAbsent(itemKey.getItem(), k -> new HashSet<>()).add(itemKey);
      }
    }

    public void consumeAvailable(AEKey key, long amount) {
      availableCounts.put(key, availableCounts.get(key) - amount);
    }
  }

  public record RecipeMatchResult(
      List<GenericStack> exactRequests,
      Set<Integer> missingIndices,
      Set<Integer> craftableIndices) {}

  public record ErrorRenderer(
      Set<Integer> missing, Set<Integer> craftable, boolean craftMissing, int color)
      implements IRecipeTransferError {
    @Override
    public @NotNull Type getType() {
      return Type.COSMETIC;
    }

    @Override
    public int getButtonHighlightColor() {
      return color;
    }

    @Override
    public void showError(
        GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        IRecipeSlotsView slots,
        int recipeX,
        int recipeY) {
      var poseStack = guiGraphics.pose();
      poseStack.pushPose();
      poseStack.translate(recipeX, recipeY, 0);

      var slotViews = slots.getSlotViews(RecipeIngredientRole.INPUT);
      for (int i = 0; i < slotViews.size(); i++) {
        boolean m = missing.contains(i);
        boolean c = craftable.contains(i);
        if (m || c) {
          slotViews
              .get(i)
              .drawHighlight(guiGraphics, m ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
        }
      }
      poseStack.popPose();
      var tooltip =
          TransferHelper.createCraftingTooltip(
              new appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots(missing, craftable),
              craftMissing);
      JEIPlugin.drawHoveringText(guiGraphics, tooltip, mouseX, mouseY);
    }
  }
}

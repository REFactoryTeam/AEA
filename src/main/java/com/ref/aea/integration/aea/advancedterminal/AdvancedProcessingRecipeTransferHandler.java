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

    boolean craftMissing = Screen.hasControlDown();

    List<GenericStack> exactRequests = new ArrayList<>();
    Set<Integer> missingIndices = new HashSet<>();
    Set<Integer> craftableIndices = new HashSet<>();

    Map<AEKey, Long> availableCounts = new HashMap<>();
    Set<AEKey> craftableKeys = new HashSet<>();

    Map<Item, Set<AEItemKey>> availableItemsMap = new HashMap<>();
    Map<Item, Set<AEItemKey>> craftableItemsMap = new HashMap<>();

    if (menu.getClientRepo() != null) {
      for (GridInventoryEntry entry : menu.getClientRepo().getAllEntries()) {
        AEKey key = entry.getWhat();
        if (entry.getStoredAmount() > 0) {
          availableCounts.put(key, entry.getStoredAmount());
          if (key instanceof AEItemKey itemKey) {
            availableItemsMap.computeIfAbsent(itemKey.getItem(), k -> new HashSet<>()).add(itemKey);
          }
        }
        if (entry.isCraftable()) {
          craftableKeys.add(key);
          if (key instanceof AEItemKey itemKey) {
            craftableItemsMap.computeIfAbsent(itemKey.getItem(), k -> new HashSet<>()).add(itemKey);
          }
        }
      }
    }

    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      var stack = player.getInventory().getItem(i);
      if (!stack.isEmpty()) {
        AEItemKey key = AEItemKey.of(stack);
        if (key != null) {
          availableCounts.merge(key, (long) stack.getCount(), Long::sum);
          availableItemsMap.computeIfAbsent(key.getItem(), k -> new HashSet<>()).add(key);
        }
      }
    }

    for (int i = 0; i < inputs.size(); i++) {
      List<GenericStack> alternatives = inputs.get(i);
      if (alternatives.isEmpty()) continue;
      GenericStack chosenExact = null;
      long neededAmount = alternatives.get(0).amount();

      for (GenericStack alt : alternatives) {
        if (availableCounts.getOrDefault(alt.what(), 0L) >= neededAmount) {
          chosenExact = new GenericStack(alt.what(), neededAmount);
          availableCounts.put(alt.what(), availableCounts.get(alt.what()) - neededAmount);
          break;
        }
      }

      if (chosenExact == null) {
        for (GenericStack alt : alternatives) {
          if (alt.what() instanceof AEItemKey altKey) {
            Set<AEItemKey> candidates =
                availableItemsMap.getOrDefault(altKey.getItem(), Collections.emptySet());
            ItemStack altStack = altKey.toStack();
            for (AEItemKey candidate : candidates) {
              if (availableCounts.getOrDefault(candidate, 0L) >= neededAmount) {
                if (stackHelper.isEquivalent(
                    candidate.toStack(), altStack, UidContext.Ingredient)) {
                  chosenExact = new GenericStack(candidate, neededAmount);
                  availableCounts.put(candidate, availableCounts.get(candidate) - neededAmount);
                  break;
                }
              }
            }
            if (chosenExact != null) break;
          }
        }
      }

      if (chosenExact == null) {
        for (GenericStack alt : alternatives) {
          if (craftableKeys.contains(alt.what())) {
            chosenExact = new GenericStack(alt.what(), neededAmount);
            craftableIndices.add(i);
            break;
          }
        }
      }

      if (chosenExact == null) {
        for (GenericStack alt : alternatives) {
          if (alt.what() instanceof AEItemKey altKey) {
            Set<AEItemKey> candidates =
                craftableItemsMap.getOrDefault(altKey.getItem(), Collections.emptySet());
            ItemStack altStack = altKey.toStack();
            for (AEItemKey candidate : candidates) {
              if (stackHelper.isEquivalent(candidate.toStack(), altStack, UidContext.Ingredient)) {
                chosenExact = new GenericStack(candidate, neededAmount);
                craftableIndices.add(i);
                break;
              }
            }
            if (chosenExact != null) break;
          }
        }
      }

      if (chosenExact == null) {
        chosenExact = new GenericStack(alternatives.get(0).what(), neededAmount);
        missingIndices.add(i);
      }

      exactRequests.add(chosenExact);
    }

    if (missingIndices.size() == inputs.size() && !inputs.isEmpty()) {
      return helper.createUserErrorForMissingSlots(
          ItemModText.NO_ITEMS.text(), display.getSlotViews(RecipeIngredientRole.INPUT));
    }

    if (doTransfer) {
      AEANetwork.INSTANCE.sendToServer(
          new AdvancedFillProcessingGridPacket(exactRequests, craftMissing));
      return null;
    }

    if (!missingIndices.isEmpty() || !craftableIndices.isEmpty()) {
      int color = !missingIndices.isEmpty() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
      return new ErrorRenderer(missingIndices, craftableIndices, craftMissing, color);
    }

    return null;
  }

  private record ErrorRenderer(
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

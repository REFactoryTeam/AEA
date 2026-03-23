package com.ref.aea.integration.aea.advancedterminal;

import static appeng.integration.modules.jeirei.TransferHelper.*;
import static com.ref.aea.core.modifier.PatternEncodingModifierService.ENTRY_COMPARATOR;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jei.JEIPlugin;
import appeng.integration.modules.jei.transfer.AbstractTransferHandler;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;
import com.ref.aea.network.AEANetwork;
import java.util.*;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class AdvancedCraftingRecipeTransferHandler<T extends AdvancedTerminalMenu>
    extends AbstractTransferHandler implements IRecipeTransferHandler<T, CraftingRecipe> {

  private final Class<T> menuClass;
  private final MenuType<T> menuType;
  private final IRecipeTransferHandlerHelper helper;

  public AdvancedCraftingRecipeTransferHandler(
      Class<T> menuClass, MenuType<T> menuType, IRecipeTransferHandlerHelper helper) {
    this.menuClass = menuClass;
    this.menuType = menuType;
    this.helper = helper;
  }

  @Override
  public @NotNull Class<T> getContainerClass() {
    return menuClass;
  }

  @Override
  public @NotNull Optional<MenuType<T>> getMenuType() {
    return Optional.of(menuType);
  }

  @Override
  public @NotNull mezz.jei.api.recipe.RecipeType<CraftingRecipe> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }

  @Override
  public IRecipeTransferError transferRecipe(
      @NotNull T menu,
      CraftingRecipe recipe,
      @NotNull IRecipeSlotsView display,
      @NotNull Player player,
      boolean maxTransfer,
      boolean doTransfer) {

    if (recipe.getType() != RecipeType.CRAFTING) return helper.createInternalError();
    if (recipe.getIngredients().isEmpty())
      return helper.createUserErrorWithTooltip(ItemModText.INCOMPATIBLE_RECIPE.text());
    if (!recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT))
      return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());

    if (menu.mode != AdvancedTerminalMode.CRAFTING) {
      menu.setMode(AdvancedTerminalMode.CRAFTING);
    }

    boolean craftMissing = Screen.hasControlDown();
    var inputSlots = display.getSlotViews(RecipeIngredientRole.INPUT);
    var slotToIngredientMap = getGuiSlotToIngredientMap(recipe);
    var missingSlots = menu.findMissingIngredients(slotToIngredientMap);

    if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
      var missingSlotViews =
          missingSlots.missingSlots().stream()
              .map(idx -> idx < inputSlots.size() ? inputSlots.get(idx) : null)
              .filter(Objects::nonNull)
              .toList();
      return helper.createUserErrorForMissingSlots(ItemModText.NO_ITEMS.text(), missingSlotViews);
    }

    if (!doTransfer) {
      if (missingSlots.totalSize() != 0) {
        int color = missingSlots.anyMissing() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
        return new ErrorRenderer(missingSlots, craftMissing, color);
      }
    } else {
      var ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
      var templateItems = NonNullList.withSize(9, ItemStack.EMPTY);
      var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
      for (int i = 0; i < ingredients.size(); i++) {
        var ingredient = ingredients.get(i);
        if (!ingredient.isEmpty()) {
          var stack =
              ingredientPriorities.entrySet().stream()
                  .filter(
                      e -> e.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                  .max(Comparator.comparingInt(Map.Entry::getValue))
                  .map(e -> ((AEItemKey) e.getKey()).toStack())
                  .orElse(ingredient.getItems()[0]);

          templateItems.set(i, stack);
        }
      }

      var recipeId = recipe.getId();
      if (menu.getPlayer().level().getRecipeManager().byKey(recipe.getId()).isEmpty()) {
        AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
        recipeId = null;
      }
      AEANetwork.INSTANCE.sendToServer(
          new AdvancedFillCraftingGridPacket(recipeId, templateItems, craftMissing));
    }

    return null;
  }

  private static Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
    var ingredients = recipe.getIngredients();
    int width = 3, height = 3;
    if (recipe instanceof ShapedRecipe shaped) {
      width = shaped.getWidth();
      height = shaped.getHeight();
    } else {
      if (ingredients.size() <= 1) width = height = 1;
      else if (ingredients.size() <= 4) width = height = 2;
    }

    var result = new LinkedHashMap<Integer, Ingredient>(ingredients.size());
    for (int i = 0; i < ingredients.size(); i++) {
      int index = getCraftingIndex(i, width, height);
      if (!ingredients.get(i).isEmpty()) result.put(index, ingredients.get(i));
    }
    return result;
  }

  private static int getCraftingIndex(int i, int width, int height) {
    int index;
    if (width == 1) {
      if (height == 3) index = (i * 3) + 1;
      else if (height == 2) index = (i * 3) + 1;
      else index = 4;
    } else if (height == 1) {
      index = i + 3;
    } else if (width == 2) {
      index = i;
      if (i > 1) {
        index++;
        if (i > 3) index++;
      }
    } else if (height == 2) {
      index = i + 3;
    } else {
      index = i;
    }
    return index;
  }

  private record ErrorRenderer(
      CraftingTermMenu.MissingIngredientSlots indices, boolean craftMissing, int color)
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
        boolean missing = indices.missingSlots().contains(i);
        boolean craftable = indices.craftableSlots().contains(i);
        if (missing || craftable) {
          slotViews
              .get(i)
              .drawHighlight(
                  guiGraphics, missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
        }
      }
      poseStack.popPose();
      var tooltip = TransferHelper.createCraftingTooltip(indices, craftMissing);
      JEIPlugin.drawHoveringText(guiGraphics, tooltip, mouseX, mouseY);
    }
  }
}

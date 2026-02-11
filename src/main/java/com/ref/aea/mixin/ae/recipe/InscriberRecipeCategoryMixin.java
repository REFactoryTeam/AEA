package com.ref.aea.mixin.ae.recipe;

import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "appeng.integration.modules.jei.InscriberRecipeCategory", remap = false)
public abstract class InscriberRecipeCategoryMixin {
  /**
   * @author RemakeFactory
   * @reason fix
   */
  @Overwrite
  public void setRecipe(IRecipeLayoutBuilder builder, InscriberRecipe recipe, IFocusGroup focuses) {
    boolean isProcessTypeInscribe = recipe.getProcessType() == InscriberProcessType.INSCRIBE;
    RecipeIngredientRole topBottomRole =
        isProcessTypeInscribe ? RecipeIngredientRole.CATALYST : RecipeIngredientRole.INPUT;
    builder.addSlot(topBottomRole, 1, 1).setSlotName("top").addIngredients(recipe.getTopOptional());
    builder
        .addSlot(RecipeIngredientRole.INPUT, 19, 24)
        .setSlotName("middle")
        .addIngredients(recipe.getMiddleInput());
    builder
        .addSlot(topBottomRole, 1, 47)
        .setSlotName("bottom")
        .addIngredients(recipe.getBottomOptional());
    builder
        .addSlot(RecipeIngredientRole.OUTPUT, 69, 25)
        .setSlotName("output")
        .addItemStack(recipe.getResultItem());
  }
}

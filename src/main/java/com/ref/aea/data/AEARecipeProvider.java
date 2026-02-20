package com.ref.aea.data;

import appeng.core.definitions.AEItems;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import com.ref.aea.core.definitions.AEAItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class AEARecipeProvider extends RecipeProvider {

  public static List<Consumer<Consumer<FinishedRecipe>>> BUILD_RECIPES = new ArrayList<>();

  public AEARecipeProvider(PackOutput pOutput) {
    super(pOutput);
  }

  @Override
  protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
    BUILD_RECIPES.forEach(consumer -> consumer.accept(pWriter));

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEAItems.MIRROR_CONNECTION_TOOL.get(), 1)
        .pattern(" IW")
        .pattern("ISI")
        .pattern(" I ")
        .define('I', Items.IRON_INGOT)
        .define('S', AEItems.SINGULARITY)
        .define('W', AEItems.WIRELESS_RECEIVER)
        .unlockedBy(getHasName(AEItems.SINGULARITY), has(AEItems.SINGULARITY))
        .save(pWriter);
  }

  @SafeVarargs
  public static void addMutualConversionRecipes(
      Supplier<? extends ItemLike> pBlockItem,
      Supplier<? extends ItemLike> pPart,
      String modid,
      TransformCircumstance circumstance,
      Supplier<Ingredient>... inputSuppliers) {
    BUILD_RECIPES.add(
        recipe -> {
          ItemLike blockItem = pBlockItem.get();
          ItemLike part = pPart.get();
          ResourceLocation altId = RecipeBuilder.getDefaultRecipeId(blockItem).withSuffix("_alt");
          Ingredient[] ingredients =
              Stream.of(inputSuppliers).map(Supplier::get).toArray(Ingredient[]::new);
          ModLoadedCondition condition = new ModLoadedCondition(modid);

          ConditionalRecipe.builder()
              .addCondition(condition)
              .addRecipe(
                  c ->
                      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, part, 1)
                          .requires(blockItem)
                          .group(null)
                          .unlockedBy(getHasName(blockItem), has(blockItem))
                          .save(c))
              .generateAdvancement()
              .build(recipe, RecipeBuilder.getDefaultRecipeId(part));

          ConditionalRecipe.builder()
              .addCondition(condition)
              .addRecipe(
                  c ->
                      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, blockItem, 1)
                          .requires(part)
                          .group(null)
                          .unlockedBy(getHasName(part), has(part))
                          .save(c, altId))
              .generateAdvancement()
              .build(recipe, altId);

          ConditionalRecipe.builder()
              .addCondition(condition)
              .addRecipe(
                  c ->
                      TransformRecipeBuilder.transform(
                          c,
                          RecipeBuilder.getDefaultRecipeId(blockItem),
                          blockItem,
                          4,
                          circumstance,
                          ingredients))
              .build(recipe, RecipeBuilder.getDefaultRecipeId(blockItem));
        });
  }

  public static Supplier<Ingredient> i(ItemLike item) {
    return i(() -> item);
  }

  public static Supplier<Ingredient> i(Supplier<? extends ItemLike> item) {
    return () -> Ingredient.of(item.get());
  }

  public static Supplier<Ingredient> t(TagKey<Item> tag) {
    return () -> Ingredient.of(tag);
  }

  public static Supplier<Ingredient> i(String itemId) {
    return () -> Ingredient.of(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId)));
  }
}

package com.ref.aea.integration.create;

import com.ref.aea.api.modifier.PatternEncodingModifier;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public enum MechanicalCraftingRecipePatternEncodingModifier
    implements PatternEncodingModifier<Boolean> {
  INSTANCE;

  static final boolean load = ModList.get().isLoaded("create");

  @Override
  public Optional<Boolean> getApplicableContext(
      ModificationResult modificationResult, ModificationContext modificationContext) {
    if (load && !(modificationContext.recipeBase() instanceof MechanicalCraftingRecipe)) {
      return Optional.empty();
    }
    return Optional.of(true);
  }

  @Override
  public ModificationResult apply(
      ModificationResult modificationResult,
      ModificationContext modificationContext,
      Boolean customContext) {
    return new ModificationResult(
        modificationResult.inputs(), modificationResult.outputs(), MergeMode.ADJACENT);
  }
}

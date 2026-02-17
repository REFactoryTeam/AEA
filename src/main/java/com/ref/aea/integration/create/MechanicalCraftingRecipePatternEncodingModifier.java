package com.ref.aea.integration.create;

import com.ref.aea.api.client.PatternEncodingModifier;
import com.ref.aea.core.modifier.PatternEncodingModifierService;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum MechanicalCraftingRecipePatternEncodingModifier
    implements PatternEncodingModifier<Boolean> {
  INSTANCE;

  @Override
  public Optional<Boolean> getApplicableContext(
      ModificationResult modificationResult, ModificationContext modificationContext) {
    if (!(modificationContext.recipeBase() instanceof MechanicalCraftingRecipe)) {
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

  public void register() {
    PatternEncodingModifierService.register(this);
  }
}

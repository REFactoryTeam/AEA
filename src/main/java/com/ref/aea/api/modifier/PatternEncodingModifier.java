package com.ref.aea.api.modifier;

import appeng.api.stacks.GenericStack;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.world.entity.player.Player;

public interface PatternEncodingModifier<C> {

  default ModificationResult modify(
      ModificationResult modificationResult, ModificationContext modificationContext) {
    return getApplicableContext(modificationResult, modificationContext)
        .map(customContext -> apply(modificationResult, modificationContext, customContext))
        .orElse(modificationResult);
  }

  Optional<C> getApplicableContext(
      ModificationResult modificationResult, ModificationContext modificationContext);

  ModificationResult apply(
      ModificationResult modificationResult,
      ModificationContext modificationContext,
      C customContext);

  record ModificationResult(
      List<List<GenericStack>> inputs, List<GenericStack> outputs, boolean mergeAdjacently) {}

  record ModificationContext(
      Object recipeBase, IRecipeSlotsView slotsView, Player player, boolean maxTransfer) {}
}

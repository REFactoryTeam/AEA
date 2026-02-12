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
      List<List<GenericStack>> inputs, List<GenericStack> outputs, MergeMode mergeMode) {}

  record ModificationContext(
      Object recipeBase, IRecipeSlotsView slotsView, Player player, boolean maxTransfer) {}

  /**
   * Defines how identical ingredients are merged during pattern encoding.
   *
   * <p>The merge mode is strictly monotonic, meaning it can only transition to a more restrictive
   * state during the modification chain: {@link #GLOBAL} -> {@link #ADJACENT} -> {@link #NONE}.
   */
  enum MergeMode {
    /**
     * Standard AE2 behavior. Consolidates all equal items into a single slot, regardless of their
     * position in the recipe.
     */
    GLOBAL,

    /**
     * Merges items only if they are sequentially adjacent in the list. Useful for recipes where
     * order matters but consecutive duplicates should be combined.
     */
    ADJACENT,

    /**
     * No merging performed. Every item defined in the list occupies a new slot (until slots run
     * out), preserving exact count and position.
     */
    NONE
  }
}

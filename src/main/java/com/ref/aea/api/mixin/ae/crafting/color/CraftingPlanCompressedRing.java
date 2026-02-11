package com.ref.aea.api.mixin.ae.crafting.color;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.ref.aea.core.color.AEANetworkCraftingProviders;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public record CraftingPlanCompressedRing(
    KeyCounter netInputs,
    KeyCounter netOutputs,
    KeyCounter catalysts,
    Map<IPatternDetails, Integer> executionRatio,
    Set<AEKey> entryPoints,
    boolean isCalculable) {

  /** 计算并缓存环的属性。 假设：默认环已经被玩家配平（各模式执行比例为 1:1）。 */
  @Nullable
  public static CraftingPlanCompressedRing calculateAndCacheRingProperties(
      int color, AEANetworkCraftingProviders providers) {

    var patternsInRing = providers.getPatternsByColor(color);
    if (patternsInRing == null || patternsInRing.isEmpty()) {
      return null;
    }

    KeyCounter totalInputs = new KeyCounter();
    KeyCounter totalOutputs = new KeyCounter();

    Map<IPatternDetails, Integer> executionRatio = new HashMap<>(patternsInRing.size());

    for (IPatternDetails pattern : patternsInRing) {
      executionRatio.put(pattern, 1);

      for (GenericStack output : pattern.getOutputs()) {
        if (output != null && output.amount() > 0) {
          totalOutputs.add(output.what(), output.amount());
        }
      }

      for (var input : pattern.getInputs()) {
        GenericStack[] possibleInputs = input.getPossibleInputs();
        if (possibleInputs != null && possibleInputs.length > 0) {
          GenericStack baseStack = possibleInputs[0];
          if (baseStack != null && baseStack.amount() > 0) {
            long requiredAmount = baseStack.amount() * input.getMultiplier();
            totalInputs.add(baseStack.what(), requiredAmount);
          }
        }
      }
    }

    KeyCounter netInputs = new KeyCounter();
    KeyCounter netOutputs = new KeyCounter();
    KeyCounter catalysts = new KeyCounter();

    Set<AEKey> allKeys = new HashSet<>(totalInputs.keySet());
    allKeys.addAll(totalOutputs.keySet());

    for (AEKey key : allKeys) {
      long in = totalInputs.get(key);
      long out = totalOutputs.get(key);

      if (in > 0 && out > 0) {
        if (out > in) {
          netOutputs.add(key, out - in);
          catalysts.add(key, in);
        } else if (in > out) {
          netInputs.add(key, in - out);
        } else {
          catalysts.add(key, 1);
        }
      } else if (in > 0) {
        netInputs.add(key, in);
      } else if (out > 0) {
        netOutputs.add(key, out);
      }
    }

    Set<AEKey> entryPoints = new HashSet<>(netOutputs.keySet());

    return new CraftingPlanCompressedRing(
        netInputs, netOutputs, catalysts, executionRatio, entryPoints, true);
  }
}

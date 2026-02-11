package com.ref.aea.mixin.ae.crafting.patterntimes;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.ref.aea.api.mixin.ae.crafting.patterntimes.IMixinPatternTimesHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingPlanSummary.class, remap = false)
public class CraftingPlanSummaryMixin {

  @Inject(method = "fromJob", at = @At("RETURN"))
  private static void populatePatternTimes(
      IGrid grid,
      IActionSource actionSource,
      ICraftingPlan job,
      CallbackInfoReturnable<CraftingPlanSummary> cir) {
    CraftingPlanSummary summary = cir.getReturnValue();

    Map<AEKey, List<Long>> timesMap = new HashMap<>();

    for (var entry : job.patternTimes().entrySet()) {
      long times = entry.getValue();
      for (var out : entry.getKey().getOutputs()) {
        timesMap.computeIfAbsent(out.what(), k -> new ArrayList<>()).add(times);
      }
    }

    for (CraftingPlanSummaryEntry entry : summary.getEntries()) {
      List<Long> times = timesMap.get(entry.getWhat());
      if (times != null && !times.isEmpty()) {
        ((IMixinPatternTimesHolder) entry).AEA$setPatternTimes(times);
      }
    }
  }
}

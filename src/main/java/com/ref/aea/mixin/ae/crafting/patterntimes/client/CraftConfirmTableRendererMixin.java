package com.ref.aea.mixin.ae.crafting.patterntimes.client;

import appeng.api.stacks.AmountFormat;
import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.ref.aea.api.mixin.ae.crafting.patterntimes.IMixinPatternTimesHolder;
import com.ref.aea.core.localization.AEAGuiText;
import com.ref.aea.util.FormatUtil;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftConfirmTableRenderer.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class CraftConfirmTableRendererMixin {

  @Inject(
      method =
          "getEntryDescription(Lappeng/menu/me/crafting/CraftingPlanSummaryEntry;)Ljava/util/List;",
      at = @At("RETURN"))
  private void addPatternTimesToDescription(
      CraftingPlanSummaryEntry entry, CallbackInfoReturnable<List<Component>> cir) {
    AEA$addPatternTimes(entry, cir, AmountFormat.SLOT, 2);
  }

  @Inject(
      method =
          "getEntryTooltip(Lappeng/menu/me/crafting/CraftingPlanSummaryEntry;)Ljava/util/List;",
      at = @At("RETURN"))
  private void addPatternTimesToTooltip(
      CraftingPlanSummaryEntry entry, CallbackInfoReturnable<List<Component>> cir) {
    AEA$addPatternTimes(entry, cir, AmountFormat.FULL, 5);
  }

  @Unique
  private void AEA$addPatternTimes(
      CraftingPlanSummaryEntry entry,
      CallbackInfoReturnable<List<Component>> cir,
      AmountFormat format,
      int maxlength) {
    List<Long> patternTimes = ((IMixinPatternTimesHolder) entry).AEA$getPatternTimes();

    if (patternTimes == null || patternTimes.isEmpty()) {
      return;
    }

    List<Component> tooltip = cir.getReturnValue();
    MutableComponent line = Component.empty();
    int totalCount = patternTimes.size();
    patternTimes.sort(Comparator.reverseOrder());
    int displayLimit = Math.min(totalCount, maxlength);
    for (int i = 0; i < displayLimit; i++) {
      if (i > 0) {
        line.append(", ");
      }
      line.append(Component.literal(FormatUtil.formatAmountFromAE(patternTimes.get(i), format)));
    }
    if (totalCount > maxlength) {
      line.append("...");
    }

    tooltip.add(AEAGuiText.PatternTimes.text(line));
  }
}

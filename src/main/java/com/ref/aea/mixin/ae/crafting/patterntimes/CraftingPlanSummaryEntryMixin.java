package com.ref.aea.mixin.ae.crafting.patterntimes;

import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.ref.aea.api.mixin.ae.crafting.patterntimes.IMixinPatternTimesHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingPlanSummaryEntry.class, remap = false)
public class CraftingPlanSummaryEntryMixin implements IMixinPatternTimesHolder {

  @Unique private List<Long> AEA$patternTimes = new ArrayList<>();

  @Override
  public void AEA$setPatternTimes(List<Long> times) {
    this.AEA$patternTimes = times;
  }

  @Override
  public List<Long> AEA$getPatternTimes() {
    return this.AEA$patternTimes;
  }

  @Inject(method = "write", at = @At("TAIL"))
  private void writePatternTimes(FriendlyByteBuf buffer, CallbackInfo ci) {
    buffer.writeVarInt(AEA$patternTimes.size());
    for (Long time : AEA$patternTimes) {
      buffer.writeVarLong(time);
    }
  }

  @Inject(method = "read", at = @At("RETURN"))
  private static void readPatternTimes(
      FriendlyByteBuf buffer, CallbackInfoReturnable<CraftingPlanSummaryEntry> cir) {
    CraftingPlanSummaryEntry entry = cir.getReturnValue();
    int size = buffer.readVarInt();
    List<Long> times = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      times.add(buffer.readVarLong());
    }

    ((IMixinPatternTimesHolder) entry).AEA$setPatternTimes(times);
  }
}

package com.ref.aea.mixin.ae.mirror;

import appeng.block.networking.CableBusColor;
import com.ref.aea.event.AEAClientModEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(value = CableBusColor.class)
public class CableBusColorMixin {
  @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
  private void onGetColor(
      BlockState state,
      BlockAndTintGetter level,
      BlockPos pos,
      int color,
      CallbackInfoReturnable<Integer> cir) {
    if (color == 542477) {
      cir.setReturnValue(AEAClientModEvent.getColorForBlockPos(state, level, pos, 1));
    }
  }
}

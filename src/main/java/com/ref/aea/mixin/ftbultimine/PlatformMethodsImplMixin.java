package com.ref.aea.mixin.ftbultimine;

import appeng.api.implementations.items.IMemoryCard;
import com.ref.aea.integration.ftbultimine.AEMemoryCardHandler;
import com.ref.aea.integration.ftbultimine.FTBUltimineServerConfigBridge;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.utils.forge.PlatformMethodsImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlatformMethodsImpl.class, remap = false)
public class PlatformMethodsImplMixin {

  @Inject(method = "blockRightClick", at = @At("HEAD"), cancellable = true)
  private static void onBlockRightClick(
      ShapeContext shapeContext,
      ServerPlayer serverPlayer,
      InteractionHand hand,
      BlockPos clickPos,
      Direction face,
      FTBUltiminePlayerData data,
      CallbackInfoReturnable<Integer> cir) {
    if (FTBUltimineServerConfigBridge.RIGHT_CLICK_MEMORY_CARD.get()
        && serverPlayer.getItemInHand(hand).getItem() instanceof IMemoryCard) {
      int result = AEMemoryCardHandler.applySettings(serverPlayer, hand, clickPos, face, data);
      if (result > 0) {
        cir.setReturnValue(result);
      }
    }
  }
}

package com.ref.aea.mixin.ae;

import appeng.client.gui.AEBaseScreen;
import com.ref.aea.util.client.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseScreen.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class AEBaseScreenMixin {
  @Inject(method = "fillRect", at = @At("HEAD"), remap = false, cancellable = true)
  private void fillRect(GuiGraphics guiGraphics, Rect2i rect, int color, CallbackInfo ci) {
    if (color == 0x8A00FF00) {
      RenderUtil.drawRainbowBorder(
          guiGraphics, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 300, 1.0f);
      ci.cancel();
    }
  }
}

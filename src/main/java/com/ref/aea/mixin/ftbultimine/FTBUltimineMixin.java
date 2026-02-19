package com.ref.aea.mixin.ftbultimine;

import com.ref.aea.integration.ftbultimine.FTBUltimineServerConfigBridge;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBUltimine.class, remap = false)
public abstract class FTBUltimineMixin {

  @Inject(method = "serverStarting", at = @At("HEAD"))
  private void beforeConfigLoad(CallbackInfo ci) {
    if (FTBUltimineServerConfigBridge.RIGHT_CLICK_MEMORY_CARD == null) {
      FTBUltimineServerConfigBridge.RIGHT_CLICK_MEMORY_CARD =
          FTBUltimineServerConfig.CONFIG
              .addBoolean("right_click_memory_card", true)
              .comment(
                  "Right-click with a Memory Card with the Ultimine key held to paste the saved configuration to multiple blocks");
    }
  }
}

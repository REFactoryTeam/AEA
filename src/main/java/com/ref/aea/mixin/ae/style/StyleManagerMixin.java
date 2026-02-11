package com.ref.aea.mixin.ae.style;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.core.AppEng;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(value = StyleManager.class, remap = false)
public abstract class StyleManagerMixin {

  @Shadow private static ResourceManager resourceManager;

  @Shadow
  private static String getBasePath(String path) {
    throw new AssertionError();
  }

  @Shadow
  private static JsonObject combineLayers(List<JsonObject> layers) {
    throw new AssertionError();
  }

  @Inject(method = "loadMergedJsonTree", at = @At("RETURN"), cancellable = true)
  private static void injectShadowIncludes(
      String path,
      Set<String> loadedFiles,
      Set<String> resourcePacks,
      CallbackInfoReturnable<JsonObject> cir) {
    String basePath = getBasePath(path);
    String fileName = path.substring(basePath.length());

    if (fileName.startsWith("includes_")) {
      return;
    }

    String shadowPath = basePath + "includes_" + fileName;

    ResourceLocation shadowId = AppEng.makeId(shadowPath.substring(1));

    resourceManager
        .getResource(shadowId)
        .ifPresent(
            resource -> {
              try (BufferedReader reader = resource.openAsReader()) {
                JsonObject shadowJson = ScreenStyle.GSON.fromJson(reader, JsonObject.class);

                if (shadowJson != null) {
                  resourcePacks.add(resource.sourcePackId());

                  List<JsonObject> layers = new ArrayList<>();
                  layers.add(cir.getReturnValue());
                  layers.add(shadowJson);

                  cir.setReturnValue(combineLayers(layers));
                }
              } catch (IOException ignored) {
              }
            });
  }
}

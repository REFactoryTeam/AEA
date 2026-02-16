package com.ref.aea.integration.create;

import com.ref.aea.core.modifier.PatternEncodingModifierService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateIntegrationClient {
  public static void init() {
    PatternEncodingModifierService.register(
        MechanicalCraftingRecipePatternEncodingModifier.INSTANCE);
  }
}

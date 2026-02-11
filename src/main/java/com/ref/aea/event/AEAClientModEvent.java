package com.ref.aea.event;

import com.ref.aea.AEA;
import com.ref.aea.core.modifier.PatternEncodingModifierService;
import com.ref.aea.integration.create.MechanicalCraftingRecipePatternEncodingModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
    modid = AEA.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public class AEAClientModEvent {

  @SubscribeEvent
  public static void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(
        () -> {
          PatternEncodingModifierService.register(
              MechanicalCraftingRecipePatternEncodingModifier.INSTANCE);
        });
  }
}

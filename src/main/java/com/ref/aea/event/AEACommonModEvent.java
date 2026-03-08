package com.ref.aea.event;

import com.ref.aea.AEA;
import com.ref.aea.network.AEANetwork;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = AEA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AEACommonModEvent {
  @SubscribeEvent
  public static void setup(final FMLCommonSetupEvent event) {
    event.enqueueWork(AEANetwork::register);
  }
}

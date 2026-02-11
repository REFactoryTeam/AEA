package com.ref.aea.config;

import com.ref.aea.AEA;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AEA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AEAServerConfig {
  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  private static final ForgeConfigSpec.BooleanValue COLOR =
      BUILDER.comment("Whether to use Color Pattern").define("Color", true);

  private static final ForgeConfigSpec.BooleanValue CPU =
      BUILDER.comment("Allows the CPU to store the final output").define("CPU", true);

  public static final ForgeConfigSpec SPEC = BUILDER.build();

  public static boolean color;
  public static boolean cpu;

  @SubscribeEvent
  static void onLoad(final ModConfigEvent event) {
    if (event.getConfig().getSpec() != SPEC) return;
    color = COLOR.get();
    cpu = CPU.get();
  }
}

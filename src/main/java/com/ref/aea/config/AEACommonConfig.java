package com.ref.aea.config;

import com.ref.aea.AEA;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AEA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AEACommonConfig {
  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  public static final ForgeConfigSpec.BooleanValue FTB_ULTIMINE;
  public static final ForgeConfigSpec.BooleanValue BUILDING_GADGETS2;
  public static final ForgeConfigSpec.BooleanValue AE_CRAFTING_BLOCKED;
  public static final ForgeConfigSpec.BooleanValue AE_CRAFTING_PATTERN_TIMES;
  public static final ForgeConfigSpec.BooleanValue AE_CRAFTING_COLOR;

  static {
    BUILDER.push("mixin");

    FTB_ULTIMINE = BUILDER.comment("Enable Mixins for FTB Ultimine").define("FTB_ULTIMINE", true);

    BUILDING_GADGETS2 =
        BUILDER.comment("Enable Mixins for Building Gadgets 2").define("BUILDING_GADGETS2", true);

    AE_CRAFTING_BLOCKED =
        BUILDER
            .comment("Enable Mixins for AE2 Crafting Blocked status")
            .define("AE_CRAFTING_BLOCKED", true);

    AE_CRAFTING_PATTERN_TIMES =
        BUILDER
            .comment("Enable Mixins for AE2 Pattern processing times")
            .define("AE_CRAFTING_PATTERN_TIMES", true);

    AE_CRAFTING_COLOR =
        BUILDER
            .comment("Enable Mixins for AE2 Crafting Color logic")
            .define("AE_CRAFTING_COLOR", true);

    BUILDER.pop();
  }

  public static final ForgeConfigSpec SPEC = BUILDER.build();
}

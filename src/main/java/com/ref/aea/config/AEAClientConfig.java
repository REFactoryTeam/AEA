package com.ref.aea.config;

import com.ref.aea.AEA;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AEA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AEAClientConfig {
  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  private static final ForgeConfigSpec.BooleanValue USE_MODIFIER =
      BUILDER
          .comment("Whether to use Encode Pattern Transfer Modifier")
          .define("useModifier", true);

  private static final ForgeConfigSpec.IntValue IO_MULTIPLIER =
      BUILDER.defineInRange("ioMultiplier", 1, 1, Integer.MAX_VALUE);

  private static final ForgeConfigSpec.ConfigValue<List<? extends String>> INPUTS_BLACK_LIST =
      BUILDER.defineListAllowEmpty("inputsBlackList", List.of(), obj -> obj instanceof String);

  private static final ForgeConfigSpec.ConfigValue<List<? extends String>> OUTPUTS_BLACK_LIST =
      BUILDER.defineListAllowEmpty("outputsBlackList", List.of(), obj -> obj instanceof String);

  private static final ForgeConfigSpec.BooleanValue IO_LOG =
      BUILDER.comment("Log IO IDs before filtering").define("logIO", false);

  private static final ForgeConfigSpec.BooleanValue DEBUG_LOG =
      BUILDER.comment("Log debug").define("debugLog", false);

  public static final ForgeConfigSpec SPEC = BUILDER.build();

  public static boolean useModifier;
  public static int ioMultiplier;
  public static boolean ioLog;
  public static boolean debugLog;
  public static Set<ResourceLocation> inputsBlackList;
  public static Set<ResourceLocation> outputsBlackList;

  @SubscribeEvent
  static void onLoad(final ModConfigEvent event) {
    if (event.getConfig().getSpec() != SPEC) return;
    useModifier = USE_MODIFIER.get();
    ioMultiplier = IO_MULTIPLIER.get();
    inputsBlackList =
        INPUTS_BLACK_LIST.get().stream().map(ResourceLocation::parse).collect(Collectors.toSet());
    outputsBlackList =
        OUTPUTS_BLACK_LIST.get().stream().map(ResourceLocation::parse).collect(Collectors.toSet());
    ioLog = IO_LOG.get();
    debugLog = DEBUG_LOG.get();
  }
}

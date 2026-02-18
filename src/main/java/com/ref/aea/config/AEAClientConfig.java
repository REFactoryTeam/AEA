package com.ref.aea.config;

import com.ref.aea.AEA;
import com.ref.aea.api.client.PatternEncodingModifier;
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

  private static final ForgeConfigSpec.EnumValue<PatternEncodingModifier.MergeMode>
      GLOBAL_MERGE_MODE =
          BUILDER
              .comment(
                  "Defines the global merge strategy for pattern encoding.",
                  "GLOBAL: Standard AE2 behavior. Merges all identical items into one slot.",
                  "ADJACENT: Merges identical items only if they are next to each other in the list.",
                  "NONE: No merging. Every item gets its own slot (until full).",
                  "Note: The merge mode is strictly monotonic (GLOBAL -> ADJACENT -> NONE). A higher strictness level set here or by other modifiers cannot be reverted.")
              .defineEnum("globalMergeMode", PatternEncodingModifier.MergeMode.GLOBAL);

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
  public static PatternEncodingModifier.MergeMode globalMergeMode;
  public static int ioMultiplier;
  public static boolean ioLog;
  public static boolean debugLog;
  public static Set<ResourceLocation> inputsBlackList;
  public static Set<ResourceLocation> outputsBlackList;

  @SubscribeEvent
  static void onLoad(final ModConfigEvent event) {
    if (event.getConfig().getSpec() != SPEC) return;
    useModifier = USE_MODIFIER.get();
    globalMergeMode = GLOBAL_MERGE_MODE.get();
    ioMultiplier = IO_MULTIPLIER.get();
    inputsBlackList =
        INPUTS_BLACK_LIST.get().stream().map(ResourceLocation::parse).collect(Collectors.toSet());
    outputsBlackList =
        OUTPUTS_BLACK_LIST.get().stream().map(ResourceLocation::parse).collect(Collectors.toSet());
    ioLog = IO_LOG.get();
    debugLog = DEBUG_LOG.get();
  }
}

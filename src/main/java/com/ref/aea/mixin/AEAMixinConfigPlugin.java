package com.ref.aea.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.FMLPaths;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class AEAMixinConfigPlugin implements IMixinConfigPlugin {

  private static boolean enableFtbUltimine = true;
  private static boolean enableBuildingGadgets2 = true;
  private static boolean enableAeCraftingBlocked = true;
  private static boolean enableAeCraftingPatternTimes = true;
  private static boolean enableAeCraftingColor = true;

  @Override
  public void onLoad(String mixinPackage) {
    final Path configPath = FMLPaths.CONFIGDIR.get().resolve("aea-common.toml");
    try {
      final CommentedFileConfig configData = CommentedFileConfig.builder(configPath).build();
      configData.load();
      enableFtbUltimine = configData.getOrElse("mixin.FTB_ULTIMINE", true);
      enableBuildingGadgets2 = configData.getOrElse("mixin.BUILDING_GADGETS2", true);
      enableAeCraftingBlocked = configData.getOrElse("mixin.AE_CRAFTING_BLOCKED", true);
      enableAeCraftingPatternTimes = configData.getOrElse("mixin.AE_CRAFTING_PATTERN_TIMES", true);
      enableAeCraftingColor = configData.getOrElse("mixin.AE_CRAFTING_COLOR", true);
      configData.close();
    } catch (Exception e) {
      System.err.println("[AEA] Failed to load mixin config manually: " + e.getMessage());
    }
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    if (mixinClassName.startsWith("com.ref.aea.mixin.ftbultimine")) {
      return enableFtbUltimine;
    }

    if (mixinClassName.startsWith("com.ref.aea.mixin.buildinggadgets2")) {
      return enableBuildingGadgets2;
    }

    // AE2 相关的细分配置
    if (mixinClassName.startsWith("com.ref.aea.mixin.ae.crafting.blocked")) {
      return enableAeCraftingBlocked;
    }

    if (mixinClassName.startsWith("com.ref.aea.mixin.ae.crafting.patterntimes")) {
      return enableAeCraftingPatternTimes;
    }

    if (mixinClassName.startsWith("com.ref.aea.mixin.ae.crafting.color")) {
      return enableAeCraftingColor;
    }

    return true;
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

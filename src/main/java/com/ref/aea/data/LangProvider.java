package com.ref.aea.data;

import appeng.core.localization.LocalizationEnum;
import com.ref.aea.AEA;
import com.ref.aea.core.localization.AEAButtonToolTips;
import com.ref.aea.core.localization.AEAGuiText;
import com.ref.aea.core.localization.AEAToolTips;
import com.ref.aea.integration.jade.MirrorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

public class LangProvider extends LanguageProvider {
  public LangProvider(PackOutput packOutput) {
    super(packOutput, AEA.MOD_ID, "en_us");
  }

  public static Map<Supplier<? extends Block>, String> Blocks = new ConcurrentHashMap<>();
  public static Map<Supplier<? extends Item>, String> Items = new ConcurrentHashMap<>();
  public static Map<Supplier<ItemStack>, String> ItemStacks = new ConcurrentHashMap<>();
  public static Map<Supplier<? extends Enchantment>, String> Enchantments =
      new ConcurrentHashMap<>();
  public static Map<Supplier<? extends MobEffect>, String> MobEffects = new ConcurrentHashMap<>();
  public static Map<Supplier<? extends EntityType<?>>, String> EntityTypes =
      new ConcurrentHashMap<>();

  @Override
  protected void addTranslations() {
    Blocks.forEach(this::addBlock);
    Items.forEach(this::addItem);
    ItemStacks.forEach(this::addItemStack);
    Enchantments.forEach(this::addEnchantment);
    MobEffects.forEach(this::addEffect);
    EntityTypes.forEach(this::addEntityType);

    this.addEnum(AEAGuiText.class);
    this.addEnum(AEAButtonToolTips.class);
    this.addEnum(AEAToolTips.class);

    this.addJadeProvider(MirrorProvider.INSTANCE.ID, "Mirror Info");
  }

  public <T extends Enum<T> & LocalizationEnum> void addEnum(Class<T> localizedEnum) {
    for (var enumConstant : localizedEnum.getEnumConstants()) {
      add(enumConstant.getTranslationKey(), enumConstant.getEnglishText());
    }
  }

  public void addJadeProvider(ResourceLocation id, String name) {
    add("config.jade.plugin_" + id.getNamespace() + "." + id.getPath(), name);
  }
}

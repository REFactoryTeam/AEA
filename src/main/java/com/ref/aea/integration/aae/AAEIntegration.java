package com.ref.aea.integration.aae;

import appeng.items.parts.PartItem;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.integration.aae.mirror.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class AAEIntegration {
  public static RegistryObject<MirrorAdvPatternProviderBlock<MirrorAdvPatternProviderEntity>>
      MIRROR_ADV_PATTERN_PROVIDER_BLOCK;
  public static RegistryObject<BlockEntityType<MirrorAdvPatternProviderEntity>>
      MIRROR_ADV_PATTERN_PROVIDER_BE;
  public static RegistryObject<PartItem<MirrorAdvPatternProviderPart>>
      MIRROR_ADV_PATTERN_PROVIDER_PART;
  public static RegistryObject<MirrorAdvPatternProviderBlock<MirrorEXAdvPatternProviderEntity>>
      MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK;
  public static RegistryObject<BlockEntityType<MirrorEXAdvPatternProviderEntity>>
      MIRROR_EX_ADV_PATTERN_PROVIDER_BE;
  public static RegistryObject<PartItem<MirrorEXAdvPatternProviderPart>>
      MIRROR_EX_ADV_PATTERN_PROVIDER_PART;

  public static void init() {
    MIRROR_ADV_PATTERN_PROVIDER_BLOCK =
        AEABlocks.block(
            "mirror_adv_pattern_provider",
            "ME Mirror Adv Pattern Provider",
            MirrorAdvPatternProviderBlock::new);

    MIRROR_ADV_PATTERN_PROVIDER_BE =
        AEABlockEntityType.create(
            "mirror_adv_pattern_provider",
            MirrorAdvPatternProviderEntity.class,
            MirrorAdvPatternProviderEntity::new,
            MIRROR_ADV_PATTERN_PROVIDER_BLOCK);

    MIRROR_ADV_PATTERN_PROVIDER_PART =
        AEAItems.createPart(
            "mirror_adv_pattern_provider_part",
            "ME Mirror Adv Pattern Provider",
            new Item.Properties(),
            MirrorAdvPatternProviderPart.class,
            MirrorAdvPatternProviderPart::new);

    MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK =
        AEABlocks.block(
            "mirror_ex_adv_pattern_provider",
            "ME Mirror Extended Adv Pattern Provider",
            MirrorAdvPatternProviderBlock::new);

    MIRROR_EX_ADV_PATTERN_PROVIDER_BE =
        AEABlockEntityType.create(
            "mirror_ex_adv_pattern_provider",
            MirrorEXAdvPatternProviderEntity.class,
            MirrorEXAdvPatternProviderEntity::new,
            MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK);

    MIRROR_EX_ADV_PATTERN_PROVIDER_PART =
        AEAItems.createPart(
            "mirror_ex_adv_pattern_provider_part",
            "ME Mirror Extended Adv Pattern Provider",
            new Item.Properties(),
            MirrorEXAdvPatternProviderPart.class,
            MirrorEXAdvPatternProviderPart::new);

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AAEIntegrationClient::init);
  }
}

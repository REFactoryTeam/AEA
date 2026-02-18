package com.ref.aea.integration.ae2;

import appeng.items.parts.PartItem;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.data.AEABlockTagsProvider;
import com.ref.aea.data.AEAItemTagsProvider;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderBlock;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderBlockEntity;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderPart;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class AE2Integration {

  public static RegistryObject<MirrorPatternProviderBlock<MirrorPatternProviderBlockEntity>>
      MIRROR_PATTERN_PROVIDER_BLOCK;
  public static RegistryObject<BlockEntityType<MirrorPatternProviderBlockEntity>>
      MIRROR_PATTERN_PROVIDER_BE;
  public static RegistryObject<PartItem<MirrorPatternProviderPart>> MIRROR_PATTERN_PROVIDER_PART;

  public static void init() {
    MIRROR_PATTERN_PROVIDER_BLOCK =
        AEABlocks.block(
            "mirror_pattern_provider",
            "ME Mirror Pattern Provider",
            MirrorPatternProviderBlock::new);

    MIRROR_PATTERN_PROVIDER_BE =
        AEABlockEntityType.create(
            "mirror_pattern_provider",
            MirrorPatternProviderBlockEntity.class,
            MirrorPatternProviderBlockEntity::new,
            MIRROR_PATTERN_PROVIDER_BLOCK);

    MIRROR_PATTERN_PROVIDER_PART =
        AEAItems.createPart(
            "mirror_pattern_provider_part",
            "ME Mirror Pattern Provider",
            new Item.Properties(),
            MirrorPatternProviderPart.class,
            MirrorPatternProviderPart::new);

    AEABlockTagsProvider.BLOCK_OPTIONAL_MAP.put(
        BlockTags.MINEABLE_WITH_PICKAXE, MIRROR_PATTERN_PROVIDER_BLOCK.getKey().location());

    AEAItemTagsProvider.ITEM_OPTIONAL_MAP.putAll(
        AEAItemTagsProvider.MIRROR_PATTERN_PROVIDER,
        List.of(
            MIRROR_PATTERN_PROVIDER_BLOCK.getKey().location(),
            MIRROR_PATTERN_PROVIDER_PART.getKey().location()));

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AE2IntegrationClient::init);
  }
}

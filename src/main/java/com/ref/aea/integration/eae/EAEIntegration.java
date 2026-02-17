package com.ref.aea.integration.eae;

import appeng.items.parts.PartItem;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.integration.eae.mirror.MirrorEXPatternProviderBlock;
import com.ref.aea.integration.eae.mirror.MirrorEXPatternProviderBlockEntity;
import com.ref.aea.integration.eae.mirror.MirrorEXPatternProviderPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class EAEIntegration {

  public static RegistryObject<MirrorEXPatternProviderBlock> MIRROR_EX_PATTERN_PROVIDER_BLOCK;
  public static RegistryObject<BlockEntityType<MirrorEXPatternProviderBlockEntity>>
      MIRROR_EX_PATTERN_PROVIDER_BE;
  public static RegistryObject<PartItem<MirrorEXPatternProviderPart>>
      MIRROR_EX_PATTERN_PROVIDER_PART;

  public static void init() {
    MIRROR_EX_PATTERN_PROVIDER_BLOCK =
        AEABlocks.block(
            "mirror_ex_pattern_provider",
            "ME Mirror Extended Pattern Provider",
            MirrorEXPatternProviderBlock::new);

    MIRROR_EX_PATTERN_PROVIDER_BE =
        AEABlockEntityType.create(
            "mirror_ex_pattern_provider",
            MirrorEXPatternProviderBlockEntity.class,
            MirrorEXPatternProviderBlockEntity::new,
            MIRROR_EX_PATTERN_PROVIDER_BLOCK);

    MIRROR_EX_PATTERN_PROVIDER_PART =
        AEAItems.createPart(
            "mirror_ex_pattern_provider_part",
            "ME Mirror Extended Pattern Provider",
            new Item.Properties(),
            MirrorEXPatternProviderPart.class,
            MirrorEXPatternProviderPart::new);
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> EAEIntegrationClient::init);
  }
}

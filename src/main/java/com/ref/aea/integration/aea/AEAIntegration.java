package com.ref.aea.integration.aea;

import appeng.api.networking.GridServices;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.data.AEABlockTagsProvider;
import com.ref.aea.integration.aea.mirror.MirrorPatternService;
import com.ref.aea.integration.aea.wireless.*;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class AEAIntegration {

  public static RegistryObject<WirelessConnectionToolItem> WIRELESS_CONNECTION_TOOL;

  public static RegistryObject<AdvancedWirelessConnectionToolItem>
      ADVANCED_WIRELESS_CONNECTION_TOOL;

  public static RegistryObject<WirelessConnectionProviderBlock> WIRELESS_CONNECTION_PROVIDER_BLOCK;

  public static RegistryObject<BlockEntityType<WirelessConnectionProviderBlockEntity>>
      WIRELESS_CONNECTION_PROVIDER_BE;

  public static RegistryObject<ExtendedWirelessConnectionProviderBlock>
      EXTENDED_WIRELESS_CONNECTION_PROVIDER_BLOCK;

  public static RegistryObject<BlockEntityType<ExtendedWirelessConnectionProviderBlockEntity>>
      EXTENDED_WIRELESS_CONNECTION_PROVIDER_BE;

  public static RegistryObject<AdvancedWirelessConnectionProviderBlock>
      ADVANCED_WIRELESS_CONNECTION_PROVIDER_BLOCK;

  public static RegistryObject<BlockEntityType<AdvancedWirelessConnectionProviderBlockEntity>>
      ADVANCED_WIRELESS_CONNECTION_PROVIDER_BE;

  public static void init() {
    WIRELESS_CONNECTION_TOOL =
        AEAItems.item(
            "wireless_connection_tool",
            "Wireless Connection Tool",
            WirelessConnectionToolItem::new);

    ADVANCED_WIRELESS_CONNECTION_TOOL =
        AEAItems.item(
            "advanced_wireless_connection_tool",
            "Advanced Wireless Connection Tool",
            AdvancedWirelessConnectionToolItem::new);

    WIRELESS_CONNECTION_PROVIDER_BLOCK =
        AEABlocks.block(
            "wireless_connection_provider",
            "Wireless Connection Provider",
            WirelessConnectionProviderBlock::new);

    WIRELESS_CONNECTION_PROVIDER_BE =
        AEABlockEntityType.create(
            "wireless_connection_provider",
            WirelessConnectionProviderBlockEntity.class,
            WirelessConnectionProviderBlockEntity::new,
            WIRELESS_CONNECTION_PROVIDER_BLOCK);

    EXTENDED_WIRELESS_CONNECTION_PROVIDER_BLOCK =
        AEABlocks.block(
            "extended_wireless_connection_provider",
            "Extended Wireless Connection Provider",
            ExtendedWirelessConnectionProviderBlock::new);

    EXTENDED_WIRELESS_CONNECTION_PROVIDER_BE =
        AEABlockEntityType.create(
            "extended_wireless_connection_provider",
            ExtendedWirelessConnectionProviderBlockEntity.class,
            ExtendedWirelessConnectionProviderBlockEntity::new,
            EXTENDED_WIRELESS_CONNECTION_PROVIDER_BLOCK);

    ADVANCED_WIRELESS_CONNECTION_PROVIDER_BLOCK =
        AEABlocks.block(
            "advanced_wireless_connection_provider",
            "Advanced Wireless Connection Provider",
            AdvancedWirelessConnectionProviderBlock::new);

    ADVANCED_WIRELESS_CONNECTION_PROVIDER_BE =
        AEABlockEntityType.create(
            "advanced_wireless_connection_provider",
            AdvancedWirelessConnectionProviderBlockEntity.class,
            AdvancedWirelessConnectionProviderBlockEntity::new,
            ADVANCED_WIRELESS_CONNECTION_PROVIDER_BLOCK);

    AEABlockTagsProvider.BLOCK_OPTIONAL_MAP.putAll(
        BlockTags.MINEABLE_WITH_PICKAXE,
        List.of(
            WIRELESS_CONNECTION_PROVIDER_BLOCK.getKey().location(),
            EXTENDED_WIRELESS_CONNECTION_PROVIDER_BLOCK.getKey().location(),
            ADVANCED_WIRELESS_CONNECTION_PROVIDER_BLOCK.getKey().location()));

    GridServices.register(IMirrorPatternService.class, MirrorPatternService.class);
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AEAIntegrationClient::init);
  }
}

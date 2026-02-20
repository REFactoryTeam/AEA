package com.ref.aea.integration.aae;

import appeng.core.definitions.AEItems;
import appeng.items.parts.PartItem;
import appeng.recipes.transform.TransformCircumstance;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.data.AEABlockTagsProvider;
import com.ref.aea.data.AEAFluidTagsProvider;
import com.ref.aea.data.AEAItemTagsProvider;
import com.ref.aea.data.AEARecipeProvider;
import com.ref.aea.integration.aae.mirror.*;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;
import net.pedroksl.advanced_ae.AdvancedAE;

public class AAEIntegration {
  public static final TagKey<Fluid> QUANTUM_INFUSION =
      TagKey.create(
          Registries.FLUID,
          ResourceLocation.fromNamespaceAndPath(AdvancedAE.MOD_ID, "quantum_infusion"));

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

    AEABlockTagsProvider.BLOCK_OPTIONAL_MAP.putAll(
        BlockTags.MINEABLE_WITH_PICKAXE,
        List.of(
            MIRROR_ADV_PATTERN_PROVIDER_BLOCK.getKey().location(),
            MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK.getKey().location()));

    AEAItemTagsProvider.ITEM_OPTIONAL_MAP.putAll(
        AEAItemTagsProvider.MIRROR_PATTERN_PROVIDER,
        List.of(
            MIRROR_ADV_PATTERN_PROVIDER_BLOCK.getKey().location(),
            MIRROR_ADV_PATTERN_PROVIDER_PART.getKey().location(),
            MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK.getKey().location(),
            MIRROR_EX_ADV_PATTERN_PROVIDER_PART.getKey().location()));

    AEAFluidTagsProvider.FLUID_OPTIONAL_MAP.putAll(
        QUANTUM_INFUSION,
        List.of(
            ResourceLocation.fromNamespaceAndPath(AdvancedAE.MOD_ID, "quantum_infusion_flowing"),
            ResourceLocation.fromNamespaceAndPath(AdvancedAE.MOD_ID, "quantum_infusion_source")));

    AEARecipeProvider.addMutualConversionRecipes(
        MIRROR_ADV_PATTERN_PROVIDER_BLOCK,
        MIRROR_ADV_PATTERN_PROVIDER_PART,
        "advanced_ae",
        TransformCircumstance.fluid(QUANTUM_INFUSION),
        AEARecipeProvider.i("advanced_ae:small_adv_pattern_provider"),
        AEARecipeProvider.i(AEItems.SINGULARITY));

    AEARecipeProvider.addMutualConversionRecipes(
        MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK,
        MIRROR_EX_ADV_PATTERN_PROVIDER_PART,
        "advanced_ae",
        TransformCircumstance.fluid(QUANTUM_INFUSION),
        AEARecipeProvider.i("advanced_ae:adv_pattern_provider"),
        AEARecipeProvider.i(AEItems.SINGULARITY));

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AAEIntegrationClient::init);
  }
}

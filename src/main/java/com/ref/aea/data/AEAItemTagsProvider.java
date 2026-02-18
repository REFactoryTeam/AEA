package com.ref.aea.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.ref.aea.AEA;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEAItemTagsProvider extends ItemTagsProvider {

  public static TagKey<Item> MIRROR_PATTERN_PROVIDER =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "mirror_pattern_provider"));

  public static Multimap<TagKey<Item>, Supplier<? extends ItemLike>> ITEM_MAP =
      LinkedHashMultimap.create();

  public static Multimap<TagKey<Item>, ResourceLocation> ITEM_OPTIONAL_MAP =
      LinkedHashMultimap.create();

  public final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

  public AEAItemTagsProvider(
      PackOutput pOutput,
      CompletableFuture<HolderLookup.Provider> pLookupProvider,
      CompletableFuture<TagLookup<Block>> pBlockTags,
      @Nullable ExistingFileHelper existingFileHelper) {
    super(pOutput, pLookupProvider, pBlockTags, AEA.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    ITEM_MAP.forEach((tagKey, i) -> this.tag(tagKey).add(i.get().asItem()));
    ITEM_OPTIONAL_MAP.forEach((tagKey, i) -> this.tag(tagKey).addOptional(i));
    tagsToCopy.forEach(this::copy);
  }
}

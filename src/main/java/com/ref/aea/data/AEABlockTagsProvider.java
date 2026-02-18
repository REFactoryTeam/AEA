package com.ref.aea.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.ref.aea.AEA;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEABlockTagsProvider extends BlockTagsProvider {

  public static Multimap<TagKey<Block>, Supplier<? extends Block>> BLOCK_MAP =
      LinkedHashMultimap.create();

  public static Multimap<TagKey<Block>, ResourceLocation> BLOCK_OPTIONAL_MAP =
      LinkedHashMultimap.create();

  public AEABlockTagsProvider(
      PackOutput output,
      CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, AEA.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    BLOCK_MAP.forEach((tagKey, b) -> this.tag(tagKey).add(b.get()));
    BLOCK_OPTIONAL_MAP.forEach((tagKey, b) -> this.tag(tagKey).addOptional(b));
  }
}

package com.ref.aea.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.ref.aea.AEA;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEAFluidTagsProvider extends FluidTagsProvider {

  public static Multimap<TagKey<Fluid>, Supplier<? extends Fluid>> FLUID_MAP =
      LinkedHashMultimap.create();

  public static Multimap<TagKey<Fluid>, ResourceLocation> FLUID_OPTIONAL_MAP =
      LinkedHashMultimap.create();

  public AEAFluidTagsProvider(
      PackOutput pOutput,
      CompletableFuture<HolderLookup.Provider> pProvider,
      @Nullable ExistingFileHelper existingFileHelper) {
    super(pOutput, pProvider, AEA.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    FLUID_MAP.forEach((tagKey, f) -> this.tag(tagKey).add(f.get()));
    FLUID_OPTIONAL_MAP.forEach((tagKey, f) -> this.tag(tagKey).addOptional(f));
  }
}

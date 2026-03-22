package com.ref.aea.data.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AEABlocksLoot extends BlockLootSubProvider {

  public static List<Supplier<? extends Block>> DROP_SELF = new ArrayList<>();

  protected AEABlocksLoot() {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags());
  }

  @Override
  protected void generate() {
    DROP_SELF.forEach(supplier -> this.dropSelf(supplier.get()));
  }

  @Override
  protected @NotNull Iterable<Block> getKnownBlocks() {
    return DROP_SELF.stream().map(Supplier::get).collect(Collectors.toList());
  }
}

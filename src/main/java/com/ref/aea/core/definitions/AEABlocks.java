package com.ref.aea.core.definitions;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import com.ref.aea.AEA;
import com.ref.aea.data.AEALangProvider;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEABlocks {
  public static final DeferredRegister<Block> DR =
      DeferredRegister.create(ForgeRegistries.BLOCKS, AEA.MOD_ID);

  public static <T extends Block> RegistryObject<T> block(
      String id, String englishName, Supplier<T> blockSupplier) {
    return block(id, englishName, blockSupplier, null, true);
  }

  public static <T extends Block> RegistryObject<T> block(
      String id,
      String englishName,
      Supplier<T> blockSupplier,
      @Nullable Function<Block, BlockItem> itemFactory,
      boolean isCreativeModeTab) {
    var deferredBlock = DR.register(id, blockSupplier);
    AEAItems.blockItem(
        id, englishName, () -> getBlockItem(id, itemFactory, deferredBlock), isCreativeModeTab);
    AEALangProvider.Blocks.put(deferredBlock, englishName);
    return deferredBlock;
  }

  private static <T extends Block> @NotNull BlockItem getBlockItem(
      String id,
      @Nullable Function<Block, BlockItem> itemFactory,
      RegistryObject<T> deferredBlock) {
    var block = deferredBlock.get();
    var itemProperties = new Item.Properties();
    if (itemFactory != null) {
      var item = itemFactory.apply(block);
      if (item == null) {
        throw new IllegalArgumentException("BlockItem factory for " + id + " returned null");
      }
      return item;
    } else if (block instanceof AEBaseBlock) {
      return new AEBaseBlockItem(block, itemProperties);
    } else {
      return new BlockItem(block, itemProperties);
    }
  }
}

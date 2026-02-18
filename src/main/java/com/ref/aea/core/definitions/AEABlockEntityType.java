package com.ref.aea.core.definitions;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.ref.aea.AEA;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AEABlockEntityType {

  public static final DeferredRegister<BlockEntityType<?>> DR =
      DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AEA.MOD_ID);

  public static <T extends AEBaseBlockEntity, B extends AEBaseEntityBlock<T>>
      RegistryObject<BlockEntityType<T>> create(
          String shortId,
          Class<T> entityClass,
          BlockEntityFactory<T> factory,
          RegistryObject<B> blockRegistryObject) {

    return DR.register(
        shortId,
        () -> {
          AtomicReference<BlockEntityType<T>> typeHolder = new AtomicReference<>();

          BlockEntityType.BlockEntitySupplier<T> supplier =
              (pos, state) -> factory.create(typeHolder.get(), pos, state);

          B block = blockRegistryObject.get();

          var type = BlockEntityType.Builder.of(supplier, block).build(null);
          typeHolder.setPlain(type);

          AEBaseBlockEntity.registerBlockEntityItem(type, block.asItem());

          BlockEntityTicker<T> serverTicker =
              ServerTickingBlockEntity.class.isAssignableFrom(entityClass)
                  ? (level, pos, state, entity) -> ((ServerTickingBlockEntity) entity).serverTick()
                  : null;

          BlockEntityTicker<T> clientTicker =
              ClientTickingBlockEntity.class.isAssignableFrom(entityClass)
                  ? (level, pos, state, entity) -> ((ClientTickingBlockEntity) entity).clientTick()
                  : null;

          block.setBlockEntity(entityClass, type, clientTicker, serverTicker);

          return type;
        });
  }

  @FunctionalInterface
  public interface BlockEntityFactory<T extends AEBaseBlockEntity> {
    T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
  }
}

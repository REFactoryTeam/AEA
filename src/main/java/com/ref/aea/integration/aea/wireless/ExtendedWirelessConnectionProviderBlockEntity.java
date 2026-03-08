package com.ref.aea.integration.aea.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.ref.aea.api.wireless.IWirelessConnectionLogic;
import com.ref.aea.api.wireless.IWirelessConnectionLogicHost;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedWirelessConnectionProviderBlockEntity extends AEBaseBlockEntity
    implements IGridConnectedBlockEntity, IWirelessConnectionLogicHost {

  private final IManagedGridNode mainNode =
      createMainNode()
          .setVisualRepresentation(getItemFromBlockEntity())
          .setInWorldNode(false)
          .setTagName("proxy")
          .setFlags(GridFlags.DENSE_CAPACITY);

  private final IManagedGridNode[] nodes = new IManagedGridNode[6];
  private final WirelessConnectionLogic[] logics = new WirelessConnectionLogic[6];

  public ExtendedWirelessConnectionProviderBlockEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);

    for (Direction dir : Direction.values()) {
      int idx = dir.ordinal();

      nodes[idx] =
          GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
              .setInWorldNode(true)
              .setTagName("proxy_" + dir.getSerializedName())
              .setFlags(GridFlags.DENSE_CAPACITY)
              .setExposedOnSides(EnumSet.of(dir))
              .setIdlePowerUsage(0);

      logics[idx] = new WirelessConnectionLogic(nodes[idx], this);
    }
  }

  @Override
  public IManagedGridNode getMainNode() {
    return this.mainNode;
  }

  protected IManagedGridNode createMainNode() {
    return GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE);
  }

  @Nullable
  @Override
  public IGridNode getGridNode(Direction dir) {
    if (dir == null) return getGridNode();
    return nodes[dir.ordinal()].getNode();
  }

  @Override
  public AECableType getCableConnectionType(Direction dir) {
    return AECableType.DENSE_SMART;
  }

  @Override
  public IWirelessConnectionLogic getLogic(@Nullable Direction direction) {
    if (direction == null) return logics[0];
    return logics[direction.ordinal()];
  }

  @Override
  public void onReady() {
    super.onReady();

    this.getMainNode().create(getLevel(), getBlockEntity().getBlockPos());

    for (IManagedGridNode node : nodes) {
      node.create(getLevel(), getBlockEntity().getBlockPos());
    }

    IGridNode gridNode = this.getMainNode().getNode();
    if (gridNode != null) {
      for (IManagedGridNode node : nodes) {
        IGridNode nodeNode = node.getNode();
        if (nodeNode != null) {
          GridHelper.createConnection(gridNode, nodeNode);
        }
      }
    }

    BlockState currentState = getBlockState();
    if (currentState.getBlock() instanceof AEBaseEntityBlock<?> block) {
      BlockState newState = block.getBlockEntityBlockState(currentState, this);
      if (currentState != newState) {
        this.markForUpdate();
      }
    }
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    this.getMainNode().destroy();
    for (IManagedGridNode node : nodes) {
      node.destroy();
    }
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    this.getMainNode().destroy();
    for (IManagedGridNode node : nodes) {
      node.destroy();
    }
  }

  @Override
  public void clearRemoved() {
    super.clearRemoved();
    scheduleInit();
  }

  @Override
  public void loadTag(CompoundTag data) {
    super.loadTag(data);
    this.getMainNode().loadFromNBT(data);
    for (Direction dir : Direction.values()) {
      int i = dir.ordinal();
      String prefix = "side_" + i + "_";

      if (data.contains(prefix + "node")) {
        nodes[i].loadFromNBT(data.getCompound(prefix + "node"));
      }

      if (data.contains(prefix + "logic")) {
        logics[i].readFromNBT(data.getCompound(prefix + "logic"));
      }
    }
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    this.getMainNode().saveToNBT(data);
    for (Direction dir : Direction.values()) {
      int i = dir.ordinal();
      String prefix = "side_" + i + "_";

      CompoundTag nodeTag = new CompoundTag();
      nodes[i].saveToNBT(nodeTag);
      data.put(prefix + "node", nodeTag);

      CompoundTag logicTag = new CompoundTag();
      logics[i].writeToNBT(logicTag);
      data.put(prefix + "logic", logicTag);
    }
  }
}

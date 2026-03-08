package com.ref.aea.integration.aea.wireless;

import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.ref.aea.api.wireless.IWirelessConnectionLogic;
import com.ref.aea.api.wireless.IWirelessConnectionLogicHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WirelessConnectionProviderBlockEntity extends AENetworkBlockEntity
    implements IWirelessConnectionLogicHost {

  protected final IWirelessConnectionLogic logic = createLogic();

  private IWirelessConnectionLogic createLogic() {
    return new WirelessConnectionLogic(this.getMainNode(), this);
  }

  public WirelessConnectionProviderBlockEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);
  }

  @Override
  public void loadTag(CompoundTag data) {
    super.loadTag(data);
    this.logic.readFromNBT(data);
  }

  @Override
  public void saveAdditional(CompoundTag data) {
    super.saveAdditional(data);
    this.logic.writeToNBT(data);
  }

  @Override
  public AECableType getCableConnectionType(Direction dir) {
    return AECableType.DENSE_SMART;
  }

  @Override
  public IWirelessConnectionLogic getLogic(@Nullable Direction direction) {
    return this.logic;
  }
}

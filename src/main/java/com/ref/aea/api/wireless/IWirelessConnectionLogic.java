package com.ref.aea.api.wireless;

import com.ref.aea.api.pos.ISidedGlobalPosHost;
import net.minecraft.nbt.CompoundTag;

public interface IWirelessConnectionLogic extends ISidedGlobalPosHost {
  void writeToNBT(CompoundTag tag);

  void readFromNBT(CompoundTag tag);

  void destroy();
}

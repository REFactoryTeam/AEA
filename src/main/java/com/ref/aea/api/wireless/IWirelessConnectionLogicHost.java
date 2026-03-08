package com.ref.aea.api.wireless;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IWirelessConnectionLogicHost {
  BlockEntity getBlockEntity();

  void saveChanges();

  IWirelessConnectionLogic getLogic(@Nullable Direction direction);
}

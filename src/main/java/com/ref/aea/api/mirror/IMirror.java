package com.ref.aea.api.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import com.ref.aea.api.pos.ISidedGlobalPosHost;
import com.ref.aea.api.pos.SidedGlobalPos;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface IMirror<T> extends IGridNodeService, ISidedGlobalPosHost {

  Optional<T> getSource();

  void updateSource(IGridNode node);

  @Override
  default @NotNull Collection<SidedGlobalPos> getSidedGlobalPos() {
    return getFirstSidedGlobalPos().map(List::of).orElse(List.of());
  }

  @Override
  default void removeSidedGlobalPos(@NotNull SidedGlobalPos pos) {
    if (getFirstSidedGlobalPos().filter(pos::equals).isPresent()) {
      clearSidedGlobalPos();
    }
  }
}

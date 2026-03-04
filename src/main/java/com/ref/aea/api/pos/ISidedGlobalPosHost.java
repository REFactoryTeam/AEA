package com.ref.aea.api.pos;

import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface ISidedGlobalPosHost {

  @NotNull
  Collection<SidedGlobalPos> getSidedGlobalPos();

  void addSidedGlobalPos(@NotNull SidedGlobalPos pos);

  void removeSidedGlobalPos(@NotNull SidedGlobalPos pos);

  void clearSidedGlobalPos();

  default Optional<SidedGlobalPos> getFirstSidedGlobalPos() {
    return getSidedGlobalPos().stream().findFirst();
  }

  default int getSidedGlobalPosCount() {
    return getSidedGlobalPos().size();
  }

  default boolean isEmpty() {
    return getSidedGlobalPos().isEmpty();
  }
}

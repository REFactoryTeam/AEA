package com.ref.aea.api.mixin.ae.crafting.blocked;

public interface IMixinBlockedAmountHolder {
  long AEA$getBlockedAmount();

  void AEA$setBlockedAmount(long blockedAmount);
}

package com.ref.aea.api.mixin.ae.crafting.mirror;

import appeng.api.stacks.AEKey;
import java.util.HashSet;

public interface IMixinAdvPatternProviderLogic {
  void AAE$setOutputCache(HashSet<AEKey> set);

  HashSet<AEKey> AAE$getOutputCache();
}

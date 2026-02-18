package com.ref.aea.api.mixin.ae.crafting.mirror;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import java.util.List;
import java.util.Set;

public interface IMixinPatternProviderLogic {

  void AEA$setPatterns(List<IPatternDetails> patterns);

  List<IPatternDetails> AEA$getPatterns();

  void AEA$setPatternInputs(Set<AEKey> patternInputs);

  Set<AEKey> AEA$getPatternInputs();

  IManagedGridNode AEA$getMainNode();
}

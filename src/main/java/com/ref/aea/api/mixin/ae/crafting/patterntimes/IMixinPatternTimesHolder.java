package com.ref.aea.api.mixin.ae.crafting.patterntimes;

import java.util.List;

public interface IMixinPatternTimesHolder {
  void AEA$setPatternTimes(List<Long> times);

  List<Long> AEA$getPatternTimes();
}

package com.ref.aea.core.cycle;

public interface IMixinPatternTermExtensions {
  void AEA$cycleProcessingOutput(boolean forward);

  void AEA$cycleProcessingInput(boolean forward);

  boolean AEA$canCycleProcessingInputs();
}

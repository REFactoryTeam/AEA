package com.ref.aea.core.localization;

import appeng.core.localization.LocalizationEnum;

public enum AEAButtonToolTips implements LocalizationEnum {
  CycleProcessingInput("Cycle Inputs"),
  CycleProcessingInputTooltip("Change the primary Input of this pattern");

  private final String englishText;

  AEAButtonToolTips(String englishText) {
    this.englishText = englishText;
  }

  public String getTranslationKey() {
    return "gui.tooltips.aea." + this.name();
  }

  public String getEnglishText() {
    return this.englishText;
  }
}

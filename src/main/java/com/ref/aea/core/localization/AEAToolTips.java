package com.ref.aea.core.localization;

import appeng.core.localization.LocalizationEnum;

public enum AEAToolTips implements LocalizationEnum {
  MirrorInfo("[%s,%s,%s] (%s) %s"),
  MirrorInfoClear("Connection cleared"),
  WirelessConnectionHostClear("Wireless Connection Host cleared"),
  WirelessConnectionAmount("%s Connected"),
  WirelessConnectionFrequency("Frequency: %s");

  private final String englishText;

  AEAToolTips(String englishText) {
    this.englishText = englishText;
  }

  public String getEnglishText() {
    return this.englishText;
  }

  public String getTranslationKey() {
    return "tooltips.aea." + this.name();
  }
}

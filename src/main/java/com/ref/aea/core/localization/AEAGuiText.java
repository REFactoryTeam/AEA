package com.ref.aea.core.localization;

import appeng.core.localization.LocalizationEnum;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public enum AEAGuiText implements LocalizationEnum {
  Blocked("Blocked"),
  PatternTimes("Pattern Times: %s");

  private final String root;

  @Nullable private final String englishText;

  private final Component text;

  AEAGuiText(@Nullable String englishText) {
    this.root = "gui.aea";
    this.englishText = englishText;
    this.text = Component.translatable(getTranslationKey());
  }

  AEAGuiText(@Nullable String englishText, String r) {
    this.root = r;
    this.englishText = englishText;
    this.text = Component.translatable(getTranslationKey());
  }

  @Nullable
  public String getEnglishText() {
    return englishText;
  }

  @Override
  public String getTranslationKey() {
    return this.root + '.' + name();
  }

  public String getLocal() {
    return text.getString();
  }
}

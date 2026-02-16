package com.ref.aea.data;

import appeng.core.localization.LocalizationEnum;
import com.ref.aea.AEA;
import com.ref.aea.core.localization.AEAButtonToolTips;
import com.ref.aea.core.localization.AEAGuiText;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class LangProvider extends LanguageProvider {
  public LangProvider(PackOutput packOutput) {
    super(packOutput, AEA.MOD_ID, "en_us");
  }

  @Override
  protected void addTranslations() {
    this.addEnum(AEAGuiText.class);
    this.addEnum(AEAButtonToolTips.class);
    this.add("tooltip.mirror.sourcePos.info", "[%s,%s,%s] (%s) %s");
  }

  public <T extends Enum<T> & LocalizationEnum> void addEnum(Class<T> localizedEnum) {
    for (var enumConstant : localizedEnum.getEnumConstants()) {
      add(enumConstant.getTranslationKey(), enumConstant.getEnglishText());
    }
  }
}

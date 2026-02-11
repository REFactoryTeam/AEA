package com.ref.aea.core.cycle;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.LocalizationEnum;
import com.ref.aea.core.localization.AEAButtonToolTips;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CycleProcessingButton extends IconButton {
  private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
  private final Icon icon;
  private final Consumer<Boolean> CONSUMER;

  public CycleProcessingButton(AEACycle action, Consumer<Boolean> consumer) {
    super(btn -> {});
    this.CONSUMER = consumer;
    LocalizationEnum displayName;
    LocalizationEnum displayValue;
    switch (action) {
      case CYCLE_PROCESSING_INPUT -> {
        icon = Icon.SCHEDULING_DEFAULT;
        displayName = AEAButtonToolTips.CycleProcessingInput;
        displayValue = AEAButtonToolTips.CycleProcessingInputTooltip;
      }
      case CYCLE_PROCESSING_OUTPUT -> {
        icon = Icon.SCHEDULING_DEFAULT;
        displayName = ButtonToolTips.CycleProcessingOutput;
        displayValue = ButtonToolTips.CycleProcessingOutputTooltip;
      }
      default -> throw new IllegalArgumentException("Unknown ActionItem: " + action);
    }

    setMessage(buildMessage(displayName, displayValue));
  }

  @Override
  public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
    if (this.active && this.visible) {
      if (this.isValidClickButton(pButton)) {
        if (this.clicked(pMouseX, pMouseY)) {
          this.playDownSound(Minecraft.getInstance().getSoundManager());
          this.onClick(pMouseX, pMouseY);
          if (Minecraft.getInstance().screen instanceof AEBaseScreen<?> aeScreen
              && aeScreen.isHandlingRightClick()) {
            CONSUMER.accept(false);
          } else {
            CONSUMER.accept(true);
          }
          return true;
        }
      }
    }
    return false;
  }

  protected Icon getIcon() {
    return this.icon;
  }

  private Component buildMessage(
      LocalizationEnum displayName, @Nullable LocalizationEnum displayValue) {
    String name = displayName.text().getString();
    if (displayValue == null) {
      return Component.literal(name);
    }
    String value = displayValue.text().getString();

    value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
    final StringBuilder sb = new StringBuilder(value);

    int i = sb.lastIndexOf("\n");
    if (i <= 0) {
      i = 0;
    }
    while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
      sb.replace(i, i + 1, "\n");
    }

    return Component.literal(name + '\n' + sb);
  }
}

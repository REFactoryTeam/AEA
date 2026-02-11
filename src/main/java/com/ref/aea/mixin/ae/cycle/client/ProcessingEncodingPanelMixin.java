package com.ref.aea.mixin.ae.cycle.client;

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import appeng.client.gui.widgets.ActionButton;
import com.ref.aea.core.cycle.AEACycle;
import com.ref.aea.core.cycle.CycleProcessingButton;
import com.ref.aea.core.cycle.IMixinPatternTermExtensions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(value = ProcessingEncodingPanel.class, remap = false)
public abstract class ProcessingEncodingPanelMixin extends EncodingModePanel {

  @Unique
  private final IMixinPatternTermExtensions AEA$menu = (IMixinPatternTermExtensions) this.menu;

  @Final @Shadow private ActionButton cycleOutputBtn;

  @Unique private CycleProcessingButton AEA$cycleInputBtn;
  @Unique private CycleProcessingButton AEA$cycleOutputBtn;

  public ProcessingEncodingPanelMixin(
      PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
    super(screen, widgets);
  }

  @Redirect(
      method = "<init>",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/client/gui/WidgetContainer;add(Ljava/lang/String;Lnet/minecraft/client/gui/components/AbstractWidget;)V"))
  private void replaceCycleButton(
      WidgetContainer instance, String tabButton, AbstractWidget abstractWidget) {
    if ("processingCycleOutput".equals(tabButton)) {
      this.AEA$cycleOutputBtn =
          new CycleProcessingButton(
              AEACycle.CYCLE_PROCESSING_OUTPUT, this.AEA$menu::AEA$cycleProcessingOutput);
      this.AEA$cycleOutputBtn.setHalfSize(true);
      widgets.add(tabButton, this.AEA$cycleOutputBtn);

      this.AEA$cycleInputBtn =
          new CycleProcessingButton(
              AEACycle.CYCLE_PROCESSING_INPUT, this.AEA$menu::AEA$cycleProcessingInput);
      this.AEA$cycleInputBtn.setHalfSize(true);
      widgets.add("processingCycleInput", this.AEA$cycleInputBtn);
      return;
    }

    widgets.add(tabButton, abstractWidget);
  }

  @Redirect(
      method = "setVisible",
      at =
          @At(
              value = "INVOKE",
              target = "Lappeng/client/gui/widgets/ActionButton;setVisibility(Z)V"))
  private void redirectSetVisibility(ActionButton instance, boolean visible) {
    if (instance == this.cycleOutputBtn) {
      if (this.AEA$cycleOutputBtn != null) {
        this.AEA$cycleOutputBtn.setVisibility(this.menu.canCycleProcessingOutputs());
      }
      if (this.AEA$cycleInputBtn != null) {
        this.AEA$cycleInputBtn.setVisibility(this.AEA$menu.AEA$canCycleProcessingInputs());
      }
    } else {
      instance.setVisibility(visible);
    }
  }
}

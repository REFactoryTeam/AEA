package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingPlanSummary;
import java.text.NumberFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/**
 * This screen shows the computed crafting plan and allows the player to select a CPU on which it
 * should be scheduled for crafting.
 */
public class AdvancedCraftConfirmScreen extends AEBaseScreen<AdvancedCraftConfirmMenu> {

  private final CraftConfirmTableRenderer table;

  private final Button start;
  private final Button selectCPU;
  private final Scrollbar scrollbar;

  public AdvancedCraftConfirmScreen(
      AdvancedCraftConfirmMenu menu,
      Inventory playerInventory,
      Component title,
      ScreenStyle style) {
    super(menu, playerInventory, title, style);
    this.table = new CraftConfirmTableRenderer(this, 9, 19);

    this.scrollbar = widgets.addScrollBar("scrollbar");

    this.start = widgets.addButton("start", GuiText.Start.text(), this::start);
    this.start.active = false;

    this.selectCPU = widgets.addButton("selectCpu", getNextCpuButtonLabel(), this::selectNextCpu);
    this.selectCPU.active = false;

    widgets.addButton("cancel", GuiText.Cancel.text(), menu::goBack);
  }

  @Override
  protected void updateBeforeRender() {
    super.updateBeforeRender();

    var errorResult = menu.submitError.result();
    if (errorResult != null && errorResult.errorCode() != null) {
      switchToScreen(
          new AdvancedCraftErrorScreen(this, errorResult.errorCode(), errorResult.errorDetail()));
      return;
    }

    this.selectCPU.setMessage(getNextCpuButtonLabel());

    CraftingPlanSummary plan = menu.getPlan();
    boolean planIsStartable = plan != null && !plan.isSimulation();
    this.start.active = !this.menu.hasNoCPU() && planIsStartable;
    this.selectCPU.active = planIsStartable;

    // Show additional status about the selected CPU and plan when the planning is done
    Component planDetails = GuiText.CalculatingWait.text();
    Component cpuDetails = Component.empty();
    if (plan != null) {
      String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
      planDetails = GuiText.BytesUsed.text(byteUsed);

      if (plan.isSimulation()) {
        cpuDetails = GuiText.PartialPlan.text();
      } else if (this.menu.getCpuAvailableBytes() > 0) {
        cpuDetails =
            GuiText.ConfirmCraftCpuStatus.text(
                this.menu.getCpuAvailableBytes(), this.menu.getCpuCoProcessors());
      } else {
        cpuDetails = GuiText.ConfirmCraftNoCpu.text();
      }
    }

    setTextContent(TEXT_ID_DIALOG_TITLE, GuiText.CraftingPlan.text(planDetails));
    setTextContent("cpu_status", cpuDetails);

    final int size = plan != null ? plan.getEntries().size() : 0;
    scrollbar.setRange(0, this.table.getScrollableRows(size), 1);
  }

  private Component getNextCpuButtonLabel() {
    if (this.menu.hasNoCPU()) {
      return GuiText.NoCraftingCPUs.text();
    }

    Component cpuName;
    if (this.menu.cpuName == null) {
      cpuName = GuiText.Automatic.text();
    } else {
      cpuName = this.menu.cpuName;
    }

    return GuiText.SelectedCraftingCPU.text(cpuName);
  }

  @Override
  public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {

    CraftingPlanSummary plan = menu.getPlan();
    if (plan != null) {
      this.table.render(
          guiGraphics, mouseX, mouseY, plan.getEntries(), scrollbar.getCurrentScroll());
    }
  }

  @org.jetbrains.annotations.Nullable
  @Override
  public StackWithBounds getStackUnderMouse(double mouseX, double mouseY) {
    var hovered = table.getHoveredStack();
    if (hovered != null) {
      return hovered;
    }
    return super.getStackUnderMouse(mouseX, mouseY);
  }

  // Allow players to confirm a craft via the enter key
  @Override
  public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      this.start();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
  }

  private void selectNextCpu() {
    getMenu().cycleSelectedCPU(!isHandlingRightClick());
  }

  private void start() {
    getMenu().startJob();
  }
}

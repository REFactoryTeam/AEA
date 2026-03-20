package com.ref.aea.integration.aea.advancedterminal;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.*;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.TabButton;
import appeng.core.AEConfig;
import appeng.menu.slot.AppEngSlot;
import com.ref.aea.integration.aea.advancedterminal.panel.AdvancedCraftingPanel;
import com.ref.aea.integration.aea.advancedterminal.panel.AdvancedModePanel;
import com.ref.aea.integration.aea.advancedterminal.panel.AdvancedProcessingPanel;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AdvancedTerminalScreen extends MEStorageScreen<AdvancedTerminalMenu> {

  private final Map<AdvancedTerminalMode, AdvancedModePanel> modePanels =
      new EnumMap<>(AdvancedTerminalMode.class);
  private final Map<AdvancedTerminalMode, TabButton> modeTabButtons =
      new EnumMap<>(AdvancedTerminalMode.class);

  public AdvancedTerminalScreen(
      AdvancedTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
    super(menu, playerInventory, title, style);

    for (var mode : AdvancedTerminalMode.values()) {
      var panel =
          switch (mode) {
            case CRAFTING -> new AdvancedCraftingPanel(this, widgets);
            case PROCESSING -> new AdvancedProcessingPanel(this, widgets);
          };
      var tabButton =
          new TabButton(
              panel.getTabIconItem(), panel.getTabTooltip(), btn -> getMenu().setMode(mode));
      tabButton.setStyle(TabButton.Style.HORIZONTAL);
      widgets.add("modePanel" + mode.ordinal(), panel);
      widgets.add("modeTabButton" + mode.ordinal(), tabButton);
      modeTabButtons.put(mode, tabButton);
      modePanels.put(mode, panel);
    }
  }

  @Override
  protected void updateBeforeRender() {
    super.updateBeforeRender();
    for (var mode : AdvancedTerminalMode.values()) {
      var selected = menu.mode == mode;
      modeTabButtons.get(mode).setSelected(selected);
      modePanels.get(mode).setVisible(selected);
    }
  }

  @Override
  public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
    super.renderSlot(guiGraphics, slot);
    if (slot instanceof AppEngSlot aeSlot && aeSlot.isActive()) {
      AdvancedTerminalMode currentMode = getMenu().mode;

      boolean isGridSlot = false;
      int slotIndex = -1;

      if (currentMode == AdvancedTerminalMode.CRAFTING && getMenu().getCraftingSlots() != null) {
        for (int i = 0; i < getMenu().getCraftingSlots().length; i++) {
          if (aeSlot == getMenu().getCraftingSlots()[i]) {
            isGridSlot = true;
            slotIndex = i;
            break;
          }
        }
      } else if (currentMode == AdvancedTerminalMode.PROCESSING
          && getMenu().getProcessingSlots() != null) {
        for (int i = 0; i < getMenu().getProcessingSlots().length; i++) {
          if (aeSlot == getMenu().getProcessingSlots()[i]) {
            isGridSlot = true;
            slotIndex = i;
            break;
          }
        }
      }

      if (isGridSlot && getMenu().isSlotWaiting(slotIndex, currentMode)) {
        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x4000FFFF);
      }
    }
  }

  @Override
  public void onClose() {
    if (AEConfig.instance().isClearGridOnClose()) {
      this.getMenu().clearGrid();
    }
    super.onClose();
  }
}

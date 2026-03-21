package com.ref.aea.integration.aea.advancedterminal.panel;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import com.ref.aea.core.localization.AEAGuiText;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdvancedProcessingPanel extends AdvancedModePanel {
  private static final Blitter BG = Blitter.texture("guis/advanced_modes.png").src(0, 60, 178, 71);

  private final Scrollbar scrollbar;
  private final ActionButton clearBtn;
  private final ActionButton clearToPlayerInvBtn;

  public AdvancedProcessingPanel(AdvancedTerminalScreen screen, WidgetContainer widgets) {
    super(screen, widgets);

    clearBtn = new ActionButton(ActionItems.STASH, btn -> menu.clearGridForMode(menu.mode));
    clearBtn.setHalfSize(true);
    widgets.add("clearProcessingBtn", clearBtn);

    clearToPlayerInvBtn =
        new ActionButton(ActionItems.STASH_TO_PLAYER_INV, btn -> menu.clearToPlayerInventory());
    clearToPlayerInvBtn.setHalfSize(true);
    widgets.add("clearProcessingToPlayerInvBtn", clearToPlayerInvBtn);

    scrollbar = widgets.addScrollBar("processingModeScrollbar", Scrollbar.SMALL);
    scrollbar.setRange(0, 9 - 3, 3);
    this.scrollbar.setCaptureMouseWheel(false);
  }

  @Override
  public void updateBeforeRender() {
    screen.repositionSlots(SlotSemantics.PROCESSING_INPUTS);

    AppEngSlot[] pSlots = menu.getProcessingSlots();
    if (pSlots.length == 0) return;

    int scroll = scrollbar.getCurrentScroll();
    int scrollOffsetY = scroll * 18;

    int viewTop = Integer.MAX_VALUE;
    for (AppEngSlot slot : pSlots) {
      viewTop = Math.min(viewTop, slot.y);
    }
    int viewBottom = viewTop + (3 * 18);

    for (AppEngSlot slot : pSlots) {
      slot.y -= scrollOffsetY;
      boolean isVisible = slot.y >= viewTop && slot.y < viewBottom;
      slot.setActive(isVisible);
    }
  }

  @Override
  public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
    BG.dest(bounds.getX() - 9, bounds.getY() + bounds.getHeight() - 164).blit(guiGraphics);
  }

  @Override
  public boolean onMouseWheel(Point mousePos, double delta) {
    return scrollbar.onMouseWheel(mousePos, delta);
  }

  @Override
  public ItemStack getTabIconItem() {
    return Items.FURNACE.getDefaultInstance();
  }

  @Override
  public Component getTabTooltip() {
    return AEAGuiText.ProcessingMode.text();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    scrollbar.setVisible(visible);
    clearBtn.setVisibility(visible);
    clearToPlayerInvBtn.setVisibility(visible);
    for (AppEngSlot slot : menu.getProcessingSlots()) {
      slot.setActive(visible);
    }
    screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
  }
}

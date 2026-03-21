package com.ref.aea.integration.aea.advancedterminal.panel;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import com.ref.aea.core.localization.AEAGuiText;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalScreen;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdvancedCraftingPanel extends AdvancedModePanel {
  private static final Blitter BG = Blitter.texture("guis/advanced_modes.png").src(0, 0, 113, 54);

  private final ToggleButton substitutionsBtn;
  private final ToggleButton fluidSubstitutionsBtn;
  private final ActionButton clearBtn;
  private final ActionButton clearToPlayerInvBtn;

  public AdvancedCraftingPanel(AdvancedTerminalScreen screen, WidgetContainer widgets) {
    super(screen, widgets);

    clearBtn = new ActionButton(ActionItems.STASH, btn -> menu.clearGridForMode(menu.mode));
    clearBtn.setHalfSize(true);
    widgets.add("clearCraftingBtn", clearBtn);

    clearToPlayerInvBtn =
        new ActionButton(ActionItems.STASH_TO_PLAYER_INV, btn -> menu.clearToPlayerInventory());
    clearToPlayerInvBtn.setHalfSize(true);
    widgets.add("clearCraftingToPlayerInvBtn", clearToPlayerInvBtn);

    this.substitutionsBtn = createCraftingSubstitutionButton(widgets);
    this.fluidSubstitutionsBtn = createCraftingFluidSubstitutionButton(widgets);
  }

  @Override
  public ItemStack getTabIconItem() {
    return Items.CRAFTING_TABLE.getDefaultInstance();
  }

  @Override
  public Component getTabTooltip() {
    return AEAGuiText.CraftingMode.text();
  }

  private ToggleButton createCraftingSubstitutionButton(WidgetContainer widgets) {
    var btn =
        new ToggleButton(
            Icon.SUBSTITUTION_ENABLED, Icon.SUBSTITUTION_DISABLED, menu::setSubstitutions);
    btn.setHalfSize(true);
    btn.setTooltipOn(
        List.of(
            ButtonToolTips.SubstitutionsOn.text(), ButtonToolTips.SubstitutionsDescEnabled.text()));
    btn.setTooltipOff(
        List.of(
            ButtonToolTips.SubstitutionsOff.text(),
            ButtonToolTips.SubstitutionsDescDisabled.text()));
    widgets.add("substitutionsBtn", btn);
    return btn;
  }

  private ToggleButton createCraftingFluidSubstitutionButton(WidgetContainer widgets) {
    var btn =
        new ToggleButton(
            Icon.FLUID_SUBSTITUTION_ENABLED,
            Icon.FLUID_SUBSTITUTION_DISABLED,
            menu::setFluidSubstitutions);
    btn.setHalfSize(true);
    btn.setTooltipOn(
        List.of(
            ButtonToolTips.FluidSubstitutions.text(),
            ButtonToolTips.FluidSubstitutionsDescEnabled.text()));
    btn.setTooltipOff(
        List.of(
            ButtonToolTips.FluidSubstitutions.text(),
            ButtonToolTips.FluidSubstitutionsDescDisabled.text()));
    widgets.add("fluidSubstitutionsBtn", btn);
    return btn;
  }

  @Override
  public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
    BG.dest(bounds.getX() + 36, bounds.getY() + bounds.getHeight() - 155).blit(guiGraphics);

    var absMouseX = bounds.getX() + mouse.getX();
    var absMouseY = bounds.getY() + mouse.getY();
    if (menu.fluidSubstitutions && fluidSubstitutionsBtn.isMouseOver(absMouseX, absMouseY)) {
      for (var slotIndex : menu.slotsSupportingFluidSubstitution) {
        drawSlotGreenBG(bounds, guiGraphics, menu.getCraftingSlots()[slotIndex]);
      }
    }
  }

  private void drawSlotGreenBG(Rect2i bounds, GuiGraphics guiGraphics, Slot slot) {
    int x = bounds.getX() + slot.x;
    int y = bounds.getY() + slot.y;
    guiGraphics.fill(x, y, x + 16, y + 16, 0x7f00FF00);
  }

  @Override
  public void updateBeforeRender() {
    this.substitutionsBtn.setState(this.menu.substitutions);
    this.fluidSubstitutionsBtn.setState(this.menu.fluidSubstitutions);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    substitutionsBtn.setVisibility(visible);
    fluidSubstitutionsBtn.setVisibility(visible);

    clearBtn.setVisibility(visible);
    clearToPlayerInvBtn.setVisibility(visible);

    menu.getOutputSlot().setActive(visible);

    for (AppEngSlot slot : menu.getCraftingSlots()) {
      slot.setActive(visible);
    }

    screen.setSlotsHidden(SlotSemantics.CRAFTING_GRID, !visible);
    screen.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !visible);
  }
}

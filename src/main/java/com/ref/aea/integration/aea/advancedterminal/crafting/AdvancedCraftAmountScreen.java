package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * When requesting to auto-craft, this dialog allows the player to enter the desired number of items
 * to craft.
 */
public class AdvancedCraftAmountScreen extends AEBaseScreen<AdvancedCraftAmountMenu> {

  private final Button next;

  private final NumberEntryWidget amountToCraft;

  private boolean amountInitialized;

  public AdvancedCraftAmountScreen(
      AdvancedCraftAmountMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
    super(menu, playerInventory, title, style);

    this.next = widgets.addButton("next", GuiText.Next.text(), this::confirm);

    AESubScreen.addBackButton(menu, "back", widgets);

    this.amountToCraft = widgets.addNumberEntryWidget("amountToCraft", NumberEntryType.UNITLESS);
    this.amountToCraft.setMinValue(1);
    this.amountToCraft.setMaxValue(Integer.MAX_VALUE);
    this.amountToCraft.setLongValue(1);
    this.amountToCraft.setTextFieldStyle(style.getWidget("amountToCraftInput"));
    this.amountToCraft.setHideValidationIcon(true);
    this.amountToCraft.setOnConfirm(this::confirm);
  }

  @Override
  protected void updateBeforeRender() {
    super.updateBeforeRender();

    if (!this.amountInitialized) {
      var whatToCraft = menu.getWhatToCraft();
      if (whatToCraft != null) {
        this.amountToCraft.setType(NumberEntryType.of(whatToCraft.what()));
        this.amountToCraft.setLongValue(whatToCraft.amount());
        this.amountInitialized = true;
      }
    }

    this.next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());
    this.next.active = this.amountToCraft.getLongValue().orElse(0) > 0;
  }

  private void confirm() {
    long amount = this.amountToCraft.getLongValue().orElse(0);
    boolean craftMissingAmount = this.amountToCraft.startsWithEquals();
    if (amount <= 0) {
      return;
    }
    menu.confirm(amount, craftMissingAmount, hasShiftDown());
  }
}

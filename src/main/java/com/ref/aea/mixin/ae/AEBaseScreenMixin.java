package com.ref.aea.mixin.ae;

import appeng.client.gui.AEBaseScreen;
import appeng.core.localization.ButtonToolTips;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.AEBaseMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import com.ref.aea.api.client.IRainbowRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseScreen.class, remap = false)
@OnlyIn(Dist.CLIENT)
public abstract class AEBaseScreenMixin<T extends AEBaseMenu> extends AbstractContainerScreen<T> {

  public AEBaseScreenMixin(T pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle);
  }

  @Inject(method = "fillRect", at = @At("HEAD"), remap = false, cancellable = true)
  private void fillRect(GuiGraphics guiGraphics, Rect2i rect, int color, CallbackInfo ci) {
    if (color == 0x8A00FF00) {
      IRainbowRender.INSTANCE.drawRainbowBorder(
          guiGraphics, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 300, 1.0f);
      ci.cancel();
    }
  }

  @ModifyArg(
      method = "renderEmptyingTooltip",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/core/localization/Tooltips;getEmptyingTooltip(Lappeng/core/localization/ButtonToolTips;Lnet/minecraft/world/item/ItemStack;Lappeng/api/behaviors/EmptyingAction;)Ljava/util/List;"),
      index = 0)
  private ButtonToolTips adjustEmptyingActionLabel(ButtonToolTips baseAction) {
    if (this.hoveredSlot instanceof AppEngSlot appEngSlot
        && appEngSlot.getInventory() instanceof ConfigMenuInventory cmi) {
      if (cmi.getDelegate().getMode() == GenericStackInv.Mode.STORAGE) {
        return ButtonToolTips.StoreAction;
      }
    }
    return baseAction;
  }
}

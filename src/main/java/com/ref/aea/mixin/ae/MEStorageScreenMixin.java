package com.ref.aea.mixin.ae;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.helpers.InventoryAction;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(value = MEStorageScreen.class)
public abstract class MEStorageScreenMixin<T extends MEStorageMenu> extends AEBaseScreen<T> {

  @Final
  @Shadow(remap = false)
  protected Repo repo;

  @Shadow(remap = false)
  protected abstract void handleGridInventoryEntryMouseClick(
      @Nullable GridInventoryEntry entry, int mouseButton, ClickType clickType);

  public MEStorageScreenMixin(
      T menu, Inventory playerInventory, Component title, ScreenStyle style) {
    super(menu, playerInventory, title, style);
  }

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void onMouseClicked(
      double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (Internal.getClientToggleState().isCheatItemsEnabled()) return;

    boolean isMiddle = Minecraft.getInstance().options.keyPickItem.matchesMouse(button);
    boolean isShift = Screen.hasShiftDown();

    if (!isShift && !isMiddle) return;

    IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
    jeiRuntime
        .getBookmarkOverlay()
        .getIngredientUnderMouse()
        .ifPresent(
            typedIngredient -> {
              GenericStack stack = GenericEntryStackHelper.ingredientToStack(typedIngredient);
              if (stack == null) return;

              GridInventoryEntry entry =
                  repo.getAllEntries().stream()
                      .filter(e -> stack.what().equals(e.getWhat()))
                      .findFirst()
                      .orElse(null);

              ClickType clickType = isShift ? ClickType.QUICK_MOVE : ClickType.CLONE;

              this.handleGridInventoryEntryMouseClick(entry, button, clickType);

              cir.setReturnValue(true);
            });
  }

  @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
  private void onMouseScrolled(
      double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
    if (Internal.getClientToggleState().isCheatItemsEnabled()
        || delta == 0
        || !Screen.hasShiftDown()) return;
    IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
    jeiRuntime
        .getBookmarkOverlay()
        .getIngredientUnderMouse()
        .ifPresent(
            typedIngredient -> {
              GenericStack stack = GenericEntryStackHelper.ingredientToStack(typedIngredient);
              if (stack == null) return;

              repo.getAllEntries().stream()
                  .filter(e -> stack.what().equals(e.getWhat()))
                  .findFirst()
                  .ifPresent(
                      entry -> {
                        InventoryAction action =
                            delta > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
                        this.menu.handleInteraction(entry.getSerial(), action);
                        cir.setReturnValue(true);
                      });
            });
  }
}

package com.ref.aea.mixin.ae.cycle;

import appeng.api.storage.ITerminalHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import com.ref.aea.core.cycle.IMixinPatternTermExtensions;
import java.util.Arrays;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public abstract class PatternEncodingTermMenuMixin extends MEStorageMenu
    implements IMixinPatternTermExtensions {

  public PatternEncodingTermMenuMixin(
      MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
    super(menuType, id, ip, host);
  }

  @Shadow public EncodingMode mode;
  @Final @Shadow private FakeSlot[] processingOutputSlots;
  @Final @Shadow private FakeSlot[] processingInputSlots;

  @Unique
  private static final String ACTION_CYCLE_PROCESSING_OUTPUT_DIR = "cycleProcessingOutputDir";

  @Unique private static final String ACTION_CYCLE_PROCESSING_INPUT = "cycleProcessingInput";

  @Inject(
      method =
          "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
      at = @At("TAIL"))
  private void initExtensions(CallbackInfo ci) {
    this.registerClientAction(
        ACTION_CYCLE_PROCESSING_OUTPUT_DIR, Boolean.class, this::AEA$cycleProcessingOutput);
    this.registerClientAction(
        ACTION_CYCLE_PROCESSING_INPUT, Boolean.class, this::AEA$cycleProcessingInput);
  }

  @Override
  public void AEA$cycleProcessingOutput(boolean forward) {
    if (isClientSide()) {
      sendClientAction(ACTION_CYCLE_PROCESSING_OUTPUT_DIR, forward);
    } else {
      if (this.mode != EncodingMode.PROCESSING) return;
      AEA$cycleSlots(this.processingOutputSlots, forward);
    }
  }

  @Override
  public void AEA$cycleProcessingInput(boolean forward) {
    if (isClientSide()) {
      sendClientAction(ACTION_CYCLE_PROCESSING_INPUT, forward);
    } else {
      if (this.mode != EncodingMode.PROCESSING) return;
      AEA$cycleSlots(this.processingInputSlots, forward);
    }
  }

  @Override
  public boolean AEA$canCycleProcessingInputs() {
    return mode == EncodingMode.PROCESSING
        && Arrays.stream(processingInputSlots).filter(s -> !s.getItem().isEmpty()).count() > 1;
  }

  @Unique
  private void AEA$cycleSlots(FakeSlot[] slots, boolean forward) {
    ItemStack[] newItems = new ItemStack[slots.length];
    int len = slots.length;

    for (int i = 0; i < len; i++) {
      newItems[i] = ItemStack.EMPTY;
      if (!slots[i].getItem().isEmpty()) {
        // Find next/prev non-empty item
        for (int offset = 1; offset < len; offset++) {
          int lookupIndex;
          if (forward) {
            lookupIndex = (i + offset) % len;
          } else {
            lookupIndex = (i - offset + len) % len;
          }

          ItemStack candidate = slots[lookupIndex].getItem();
          if (!candidate.isEmpty()) {
            newItems[i] = candidate;
            break;
          }
        }
      }
    }
    for (int i = 0; i < len; i++) {
      slots[i].set(newItems[i]);
    }
  }
}

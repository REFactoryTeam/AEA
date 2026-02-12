package com.ref.aea.mixin.buildinggadgets2;

import appeng.core.definitions.AEItems;
import com.direwolf20.buildinggadgets2.common.containers.customhandler.TemplateManagerHandler;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TemplateManagerHandler.class, remap = false)
public abstract class TemplateManagerHandlerMixin {

  @Inject(
      method = "isItemValid(ILnet/minecraft/world/item/ItemStack;)Z",
      at = @At("RETURN"),
      cancellable = true)
  private void addAE2PatternsValidation(
      int slot, @Nonnull ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValue()) {
      return;
    }
    if (slot == 1
        && (stack.is(AEItems.PROCESSING_PATTERN.asItem())
            || stack.is(AEItems.BLANK_PATTERN.asItem()))) {
      cir.setReturnValue(true);
    }
  }
}

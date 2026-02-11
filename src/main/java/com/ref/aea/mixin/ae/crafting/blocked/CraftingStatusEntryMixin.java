package com.ref.aea.mixin.ae.crafting.blocked;

import appeng.menu.me.crafting.CraftingStatusEntry;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinBlockedAmountHolder;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingStatusEntry.class, remap = false)
public class CraftingStatusEntryMixin implements IMixinBlockedAmountHolder {

  @Unique private long AEA$blockedAmount;

  @Override
  public long AEA$getBlockedAmount() {
    return this.AEA$blockedAmount;
  }

  @Override
  public void AEA$setBlockedAmount(long blockedAmount) {
    this.AEA$blockedAmount = blockedAmount;
  }

  @Inject(method = "write", at = @At("TAIL"))
  private void writeBlocked(FriendlyByteBuf buffer, CallbackInfo ci) {
    buffer.writeVarLong(this.AEA$blockedAmount);
  }

  @Inject(method = "read", at = @At("RETURN"))
  private static void readBlocked(
      FriendlyByteBuf buffer, CallbackInfoReturnable<CraftingStatusEntry> cir) {
    CraftingStatusEntry entry = cir.getReturnValue();
    if (entry instanceof IMixinBlockedAmountHolder holder) {
      holder.AEA$setBlockedAmount(buffer.readVarLong());
    }
  }
}

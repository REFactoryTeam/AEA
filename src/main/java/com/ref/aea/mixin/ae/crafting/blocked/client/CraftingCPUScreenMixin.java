package com.ref.aea.mixin.ae.crafting.blocked.client;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.menu.me.crafting.CraftingStatusEntry;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinBlockedAmountHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftingCPUScreen.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class CraftingCPUScreenMixin {

  @Unique private long AEA$capturedBlockedAmount = 0;

  @Redirect(
      method = "postUpdate",
      at =
          @At(
              value = "INVOKE",
              target = "Lappeng/menu/me/crafting/CraftingStatusEntry;getPendingAmount()J"))
  private long captureBlockedAmountFromEntry(CraftingStatusEntry instance) {
    if (instance instanceof IMixinBlockedAmountHolder holder) {
      this.AEA$capturedBlockedAmount = holder.AEA$getBlockedAmount();
    } else {
      this.AEA$capturedBlockedAmount = 0;
    }
    return instance.getPendingAmount();
  }

  @Redirect(
      method = "postUpdate",
      at = @At(value = "NEW", target = "appeng/menu/me/crafting/CraftingStatusEntry"))
  private CraftingStatusEntry constructWithBlockedAmount(
      long serial, AEKey what, long storedAmount, long activeAmount, long pendingAmount) {

    CraftingStatusEntry newEntry =
        new CraftingStatusEntry(serial, what, storedAmount, activeAmount, pendingAmount);

    if (newEntry instanceof IMixinBlockedAmountHolder holder) {
      holder.AEA$setBlockedAmount(this.AEA$capturedBlockedAmount);
    }

    return newEntry;
  }
}

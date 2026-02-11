package com.ref.aea.mixin.ae.crafting.blocked;

import appeng.api.stacks.AEKey;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.crafting.CraftingStatus;
import appeng.menu.me.crafting.CraftingStatusEntry;
import com.google.common.collect.ImmutableList;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinBlockedAmountHolder;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinCraftingCpuLogicBlocked;
import java.util.Iterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = CraftingStatus.class, remap = false)
public class CraftingStatusMixin {

  @Inject(
      method = "create",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;",
              shift = At.Shift.BEFORE),
      locals = LocalCapture.CAPTURE_FAILHARD)
  private static void ae2Blocked$captureBlockedData(
      IncrementalUpdateHelper changes,
      CraftingCpuLogic logic,
      CallbackInfoReturnable<CraftingStatus> cir,
      boolean full,
      ImmutableList.Builder<CraftingStatusEntry> newEntries,
      Iterator<AEKey> var4,
      AEKey what,
      long storedCount,
      long activeCount,
      long pendingCount,
      AEKey sentStack,
      CraftingStatusEntry entry) {
    if (logic instanceof IMixinCraftingCpuLogicBlocked blockedLogic
        && entry instanceof IMixinBlockedAmountHolder holder) {
      holder.AEA$setBlockedAmount(blockedLogic.AEA$getBlocked(what));
    }
  }
}

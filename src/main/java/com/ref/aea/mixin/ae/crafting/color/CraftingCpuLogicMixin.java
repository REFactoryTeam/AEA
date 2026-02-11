package com.ref.aea.mixin.ae.crafting.color;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import com.ref.aea.config.AEAServerConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCpuLogicMixin {

  @Shadow private ExecutingCraftingJob job;

  @Shadow @Final private ListCraftingInventory inventory;

  @Redirect(
      method = "insert",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/crafting/CraftingLink;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J"))
  private long redirectLinkInsertToInternalInventory(
      CraftingLink instance, AEKey what, long amount, Actionable type) {
    if (AEAServerConfig.cpu) {
      this.inventory.insert(what, amount, type);
      return amount;
    } else {
      return ((ExecutingCraftingJobAccessor) this.job).getLink().insert(what, amount, type);
    }
  }

  @Inject(method = "finishJob", at = @At("HEAD"))
  private void pushFinalOutputOnFinish(boolean success, CallbackInfo ci) {
    if (AEAServerConfig.cpu) {
      AEKey finalKey = ((ExecutingCraftingJobAccessor) this.job).getFinalOutput().what();
      long available = this.inventory.extract(finalKey, Long.MAX_VALUE, Actionable.SIMULATE);
      if (available <= 0) return;
      long accepted =
          ((ExecutingCraftingJobAccessor) this.job)
              .getLink()
              .insert(finalKey, available, Actionable.MODULATE);
      if (accepted > 0) {
        this.inventory.extract(finalKey, accepted, Actionable.MODULATE);
      }
    }
  }
}

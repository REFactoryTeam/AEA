package com.ref.aea.mixin.ae.crafting.blocked;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.service.CraftingService;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinCraftingCpuLogicBlocked;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCpuLogicMixin implements IMixinCraftingCpuLogicBlocked {

  @Shadow
  protected abstract void postChange(AEKey key);

  @Unique private final KeyCounter AEA$blockedTasks = new KeyCounter();
  @Unique private IPatternDetails AEA$currentDetails;
  @Unique private boolean AEA$pushedSuccessThisPattern;

  @Override
  public long AEA$getBlocked(AEKey key) {
    return AEA$blockedTasks.get(key);
  }

  @Inject(method = "executeCrafting", at = @At("HEAD"))
  private void AEA$clearBlockedAtStart(
      int maxPatterns,
      CraftingService craftingService,
      IEnergyService energyService,
      Level level,
      CallbackInfoReturnable<Integer> cir) {
    AEA$blockedTasks.reset();
    AEA$currentDetails = null;
  }

  @ModifyVariable(method = "executeCrafting", at = @At(value = "STORE"), name = "details")
  private IPatternDetails AEA$capturePatternAndResetFlag(IPatternDetails details) {
    this.AEA$currentDetails = details;
    this.AEA$pushedSuccessThisPattern = false;
    return details;
  }

  @Redirect(
      method = "executeCrafting",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"))
  private boolean AEA$detectPushSuccess(
      ICraftingProvider instance, IPatternDetails iPatternDetails, KeyCounter[] keyCounters) {
    boolean success = instance.pushPattern(iPatternDetails, keyCounters);
    if (success) {
      this.AEA$pushedSuccessThisPattern = true;
    }
    return success;
  }

  @Inject(
      method = "executeCrafting",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/crafting/execution/CraftingCpuHelper;reinjectPatternInputs(Lappeng/crafting/inv/ICraftingInventory;[Lappeng/api/stacks/KeyCounter;)V"))
  private void AEA$handleReinjectAndBlock(
      int maxPatterns,
      CraftingService craftingService,
      IEnergyService energyService,
      Level level,
      CallbackInfoReturnable<Integer> cir) {
    if (!this.AEA$pushedSuccessThisPattern && this.AEA$currentDetails != null) {
      for (var output : this.AEA$currentDetails.getOutputs()) {
        this.AEA$blockedTasks.add(output.what(), output.amount());
        this.postChange(output.what());
      }
    }
  }
}

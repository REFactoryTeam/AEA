package com.ref.aea.mixin.ae.crafting.blocked.client;

import appeng.api.util.AEColor;
import appeng.client.gui.me.crafting.CraftingStatusTableRenderer;
import appeng.core.AEConfig;
import appeng.menu.me.crafting.CraftingStatusEntry;
import com.ref.aea.api.mixin.ae.crafting.blocked.IMixinBlockedAmountHolder;
import com.ref.aea.core.localization.AEAGuiText;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingStatusTableRenderer.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class CraftingStatusTableRendererMixin {

  @Shadow @Final private static int BACKGROUND_ALPHA;

  @Inject(method = "getEntryDescription*", at = @At("RETURN"))
  private void addBlockedToDescription(
      CraftingStatusEntry entry, CallbackInfoReturnable<List<Component>> cir) {
    AEA$addBlockedAmount(entry, cir);
  }

  @Inject(method = "getEntryTooltip*", at = @At("RETURN"))
  private void addBlockedToTooltip(
      CraftingStatusEntry entry, CallbackInfoReturnable<List<Component>> cir) {
    AEA$addBlockedAmount(entry, cir);
  }

  @Unique
  private void AEA$addBlockedAmount(
      CraftingStatusEntry entry, CallbackInfoReturnable<List<Component>> cir) {
    if (entry instanceof IMixinBlockedAmountHolder holder && holder.AEA$getBlockedAmount() > 0) {
      List<Component> lines = cir.getReturnValue();
      lines.add(AEAGuiText.Blocked.text());
    }
  }

  @Inject(method = "getEntryBackgroundColor*", at = @At("HEAD"), cancellable = true)
  private void setBlockedBackgroundColor(
      CraftingStatusEntry entry, CallbackInfoReturnable<Integer> cir) {
    if (AEConfig.instance().isUseColoredCraftingStatus()) {
      if (entry instanceof IMixinBlockedAmountHolder holder && holder.AEA$getBlockedAmount() > 0) {
        int color = AEColor.ORANGE.blackVariant | BACKGROUND_ALPHA;
        cir.setReturnValue(color);
      }
    }
  }
}

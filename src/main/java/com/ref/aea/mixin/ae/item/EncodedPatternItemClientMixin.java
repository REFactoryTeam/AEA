package com.ref.aea.mixin.ae.item;

import appeng.crafting.pattern.EncodedPatternItem;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EncodedPatternItem.class)
@OnlyIn(Dist.CLIENT)
public class EncodedPatternItemClientMixin {

  @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
  private void onAppendHoverText(
      ItemStack stack,
      Level level,
      List<Component> lines,
      TooltipFlag advancedTooltips,
      CallbackInfo ci) {
    CompoundTag rootTag = stack.getTag();
    if (rootTag != null && rootTag.size() == 1 && rootTag.contains("display", Tag.TAG_COMPOUND)) {
      ci.cancel();
    }
  }
}

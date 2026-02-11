package com.ref.aea.mixin.ae.crafting.color;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import com.ref.aea.api.mixin.ae.crafting.color.IMixinPatternDetails;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = IPatternDetails.class, remap = false)
public interface IPatternDetailsMixin extends IMixinPatternDetails {
  @Shadow
  AEItemKey getDefinition();

  @Unique
  default int AEA$getColor() {
    ItemStack definitionStack = getDefinition().toStack();
    if (definitionStack.isEmpty()
        || !(definitionStack.getItem() instanceof DyeableLeatherItem dyeableLeatherItem)) {
      return -1;
    }
    return dyeableLeatherItem.getColor(definitionStack);
  }
}

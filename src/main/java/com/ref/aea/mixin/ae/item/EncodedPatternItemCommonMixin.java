package com.ref.aea.mixin.ae.item;

import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EncodedPatternItem.class)
public abstract class EncodedPatternItemCommonMixin implements DyeableLeatherItem {
  @Override
  public int getColor(ItemStack stack) {
    CompoundTag compoundtag = stack.getTagElement("display");
    return compoundtag != null && compoundtag.contains("color", Tag.TAG_ANY_NUMERIC)
        ? compoundtag.getInt("color")
        : -1;
  }
}

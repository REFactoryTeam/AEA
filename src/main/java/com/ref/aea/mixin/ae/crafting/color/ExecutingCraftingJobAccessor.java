package com.ref.aea.mixin.ae.crafting.color;

import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface ExecutingCraftingJobAccessor {
  @Accessor("finalOutput")
  GenericStack getFinalOutput();

  @Accessor("link")
  CraftingLink getLink();
}

package com.ref.aea.mixin.aae.crafting.color;

import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface AdvExecutingCraftingJobAccessor {
  @Accessor("finalOutput")
  GenericStack getFinalOutput();

  @Accessor("link")
  CraftingLink getLink();
}

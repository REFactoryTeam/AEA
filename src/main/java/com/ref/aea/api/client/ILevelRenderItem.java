package com.ref.aea.api.client;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface ILevelRenderItem {
  @OnlyIn(Dist.CLIENT)
  void renderLevelOverlay(RenderLevelStageEvent event, ItemStack stack);
}

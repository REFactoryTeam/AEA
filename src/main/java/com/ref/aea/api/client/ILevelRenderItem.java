package com.ref.aea.api.client;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface ILevelRenderItem {
  void renderLevelOverlay(RenderLevelStageEvent event, ItemStack stack);
}

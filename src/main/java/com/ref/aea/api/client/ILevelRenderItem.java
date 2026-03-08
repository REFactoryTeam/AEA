package com.ref.aea.api.client;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

public interface ILevelRenderItem {
  @OnlyIn(Dist.CLIENT)
  void renderLevelOverlay(@NotNull RenderLevelStageEvent event, @NotNull ItemStack stack);
}

package com.ref.aea.api.client;

import com.ref.aea.util.client.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IRainbowRender {
  IRainbowRender INSTANCE = new RenderUtil();

  void drawRainbowBorder(
      GuiGraphics guiGraphics, int x, int y, int width, int height, float z, float bw);
}

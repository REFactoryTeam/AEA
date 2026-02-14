package com.ref.aea.util.client;

import com.mojang.blaze3d.vertex.*;
import com.ref.aea.api.IRainbowRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class RenderUtil implements IRainbowRender {

  public RenderUtil() {}

  @Override
  public void drawRainbowBorder(
      GuiGraphics guiGraphics, int x, int y, int width, int height, float z, float bw) {
    VertexConsumer bufferBuilder = guiGraphics.bufferSource().getBuffer(RenderType.gui());
    Matrix4f matrix = guiGraphics.pose().last().pose();

    long time = System.currentTimeMillis();

    int c1 = getRainbowColor(time, 0.0f);
    int c2 = getRainbowColor(time, 0.25f);
    int c3 = getRainbowColor(time, 0.5f);
    int c4 = getRainbowColor(time, 0.75f);

    float x1 = (float) x;
    float y1 = (float) y;
    float x2 = x1 + width;
    float y2 = y1 + height;

    float x1In = x1 + bw;
    float y1In = y1 + bw;
    float x2In = x2 - bw;
    float y2In = y2 - bw;

    // Top
    addGuiRect(bufferBuilder, matrix, x1, y1, x2, y1In, z, c1, c2, c2, c1);
    // Right
    addGuiRect(bufferBuilder, matrix, x2In, y1In, x2, y2In, z, c2, c2, c3, c3);
    // Bottom
    addGuiRect(bufferBuilder, matrix, x1, y2In, x2, y2, z, c4, c3, c3, c4);
    // Left
    addGuiRect(bufferBuilder, matrix, x1, y1In, x1In, y2In, z, c1, c1, c4, c4);
  }

  private int getRainbowColor(long time, float offset) {
    float hue = ((time % 3000L) / 3000.0f + offset) % 1.0f;
    return Mth.hsvToRgb(hue, 0.8f, 1.0f) | 0xFF000000;
  }

  private void addGuiRect(
      VertexConsumer buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float x2,
      float y2,
      float z,
      int c1,
      int c2,
      int c3,
      int c4) {
    buffer.vertex(matrix, x1, y2, z).color(c4).endVertex();
    buffer.vertex(matrix, x2, y2, z).color(c3).endVertex();
    buffer.vertex(matrix, x2, y1, z).color(c2).endVertex();
    buffer.vertex(matrix, x1, y1, z).color(c1).endVertex();
  }
}

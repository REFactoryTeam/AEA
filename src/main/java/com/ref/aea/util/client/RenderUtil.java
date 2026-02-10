package com.ref.aea.util.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class RenderUtil {

  private RenderUtil() {}

  public static void drawRainbowBorder(
      GuiGraphics guiGraphics, int x, int y, int width, int height, float z, float bw) {

    VertexConsumer bufferBuilder = guiGraphics.bufferSource().getBuffer(RenderType.gui());
    Matrix4f matrix = guiGraphics.pose().last().pose();

    long time = System.currentTimeMillis();

    int c1 = getDynamicRainbowColor(time, 0.0f);
    int c2 = getDynamicRainbowColor(time, 0.25f);
    int c3 = getDynamicRainbowColor(time, 0.5f);
    int c4 = getDynamicRainbowColor(time, 0.75f);

    float xF = (float) x;
    float yF = (float) y;
    float wF = (float) width;
    float hF = (float) height;

    addRect(bufferBuilder, matrix, xF, yF, xF + wF, yF + bw, z, c1, c2, c2, c1);

    addRect(bufferBuilder, matrix, xF + wF - bw, yF + bw, xF + wF, yF + hF - bw, z, c2, c2, c3, c3);

    addRect(bufferBuilder, matrix, xF, yF + hF - bw, xF + wF, yF + hF, z, c4, c3, c3, c4);

    addRect(bufferBuilder, matrix, xF, yF + bw, xF + bw, yF + hF - bw, z, c1, c1, c4, c4);
  }

  private static void addRect(
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
    addVertex(buffer, matrix, x1, y2, z, c4);
    addVertex(buffer, matrix, x2, y2, z, c3);
    addVertex(buffer, matrix, x2, y1, z, c2);
    addVertex(buffer, matrix, x1, y1, z, c1);
  }

  private static void addVertex(
      VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int color) {
    buffer.vertex(matrix, x, y, z).color(color).endVertex();
  }

  private static int getDynamicRainbowColor(long time, float offset) {
    float cycleMillis = 4000.0f;
    float hue = ((time % (long) cycleMillis) / cycleMillis + offset) % 1.0f;
    int rgb = Mth.hsvToRgb(hue, 0.8f, 1.0f);
    return 0xFF000000 | rgb;
  }
}

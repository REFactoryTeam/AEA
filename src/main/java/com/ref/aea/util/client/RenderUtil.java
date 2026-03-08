package com.ref.aea.util.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.ref.aea.api.client.IRainbowRender;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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

  @Override
  public void drawWorldRainbowOutline(AABB box, RenderLevelStageEvent event) {
    RenderSystem.disableDepthTest();
    RenderSystem.enableBlend();

    PoseStack stack = event.getPoseStack();
    Vec3 cameraPos = event.getCamera().getPosition();
    MultiBufferSource.BufferSource bufferSource =
        Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.RAINBOW_LINES);

    stack.pushPose();
    stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    Matrix4f matrix = stack.last().pose();

    long time = System.currentTimeMillis();

    float minX = (float) box.minX;
    float minY = (float) box.minY;
    float minZ = (float) box.minZ;
    float maxX = (float) box.maxX;
    float maxY = (float) box.maxY;
    float maxZ = (float) box.maxZ;

    int c1 = getRainbowColor(time, 0.0f);
    int c2 = getRainbowColor(time, 0.25f);
    int c3 = getRainbowColor(time, 0.5f);
    int c4 = getRainbowColor(time, 0.75f);

    addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, c1, c2, 0, 0, 1);
    addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, c1, c2, 0, 0, 1);
    addLine(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, c4, c3, 0, 0, 1);
    addLine(buffer, matrix, minX, maxY, maxZ, maxX, maxY, maxZ, c4, c3, 0, 0, 1);

    addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, c1, c1, 0, 1, 0);
    addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, c2, c2, 0, 1, 0);
    addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, c3, c3, 0, 1, 0);
    addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, c4, c4, 0, 1, 0);

    addLine(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, c1, c4, 0, 0, 1);
    addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, c2, c3, 0, 0, 1);
    addLine(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, c1, c4, 0, 0, 1);
    addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, c2, c3, 0, 0, 1);

    stack.popPose();

    bufferSource.endBatch(CustomRenderType.RAINBOW_LINES);
    RenderSystem.enableDepthTest();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
  }

  @Override
  public void drawWorldRainbowFill(AABB box, RenderLevelStageEvent event, float alpha) {
    RenderSystem.disableDepthTest();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();

    PoseStack stack = event.getPoseStack();
    Vec3 cameraPos = event.getCamera().getPosition();
    MultiBufferSource.BufferSource bufferSource =
        Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.RAINBOW_QUADS);

    stack.pushPose();
    stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    Matrix4f matrix = stack.last().pose();

    long time = System.currentTimeMillis();

    int c1 = applyAlpha(getRainbowColor(time, 0.0f), alpha);
    int c2 = applyAlpha(getRainbowColor(time, 0.25f), alpha);
    int c3 = applyAlpha(getRainbowColor(time, 0.5f), alpha);
    int c4 = applyAlpha(getRainbowColor(time, 0.75f), alpha);

    float minX = (float) box.minX;
    float minY = (float) box.minY;
    float minZ = (float) box.minZ;
    float maxX = (float) box.maxX;
    float maxY = (float) box.maxY;
    float maxZ = (float) box.maxZ;

    // Bottom
    addQuad(
        buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, c1,
        c2, c3, c4);
    // Top
    addQuad(
        buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, c1,
        c4, c3, c2);
    // North (Z-)
    addQuad(
        buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, c1,
        c1, c2, c2);
    // South (Z+)
    addQuad(
        buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, c4,
        c3, c3, c4);
    // West (X-)
    addQuad(
        buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, c1,
        c4, c4, c1);
    // East (X+)
    addQuad(
        buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, c2,
        c2, c3, c3);

    stack.popPose();

    bufferSource.endBatch(CustomRenderType.RAINBOW_QUADS);
    RenderSystem.enableDepthTest();
    RenderSystem.disableBlend();
  }

  @Override
  public void drawWorldRainbowLine(Vec3 v1, Vec3 v2, RenderLevelStageEvent event) {
    RenderSystem.disableDepthTest();
    RenderSystem.enableBlend();

    PoseStack stack = event.getPoseStack();
    Vec3 cameraPos = event.getCamera().getPosition();
    MultiBufferSource.BufferSource bufferSource =
        Minecraft.getInstance().renderBuffers().bufferSource();
    VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.RAINBOW_LINES);

    stack.pushPose();
    stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    Matrix4f matrix = stack.last().pose();

    Vec3 delta = v2.subtract(v1);
    Vec3 normal = delta.normalize();

    float nx = (delta.lengthSqr() > 1e-6) ? (float) normal.x : 0f;
    float ny = (delta.lengthSqr() > 1e-6) ? (float) normal.y : 1f;
    float nz = (delta.lengthSqr() > 1e-6) ? (float) normal.z : 0f;

    long time = System.currentTimeMillis();
    int c1 = getRainbowColor(time, 0.0f);
    int c2 = getRainbowColor(time, 0.5f);

    addLine(
        buffer,
        matrix,
        (float) v1.x,
        (float) v1.y,
        (float) v1.z,
        (float) v2.x,
        (float) v2.y,
        (float) v2.z,
        c1,
        c2,
        nx,
        ny,
        nz);

    stack.popPose();
    bufferSource.endBatch(CustomRenderType.RAINBOW_LINES);

    RenderSystem.enableDepthTest();
  }

  public int getRainbowColor(long time, float offset) {
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

  private void addLine(
      VertexConsumer buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      int colorStart,
      int colorEnd,
      float nx,
      float ny,
      float nz) {
    buffer.vertex(matrix, x1, y1, z1).color(colorStart).normal(nx, ny, nz).endVertex();
    buffer.vertex(matrix, x2, y2, z2).color(colorEnd).normal(nx, ny, nz).endVertex();
  }

  private int applyAlpha(int color, float alpha) {
    int a = (int) (alpha * 255.0F) << 24;
    return (color & 0x00FFFFFF) | a;
  }

  private void addQuad(
      VertexConsumer buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int c1,
      int c2,
      int c3,
      int c4) {
    buffer.vertex(matrix, x1, y1, z1).color(c1).endVertex();
    buffer.vertex(matrix, x2, y2, z2).color(c2).endVertex();
    buffer.vertex(matrix, x3, y3, z3).color(c3).endVertex();
    buffer.vertex(matrix, x4, y4, z4).color(c4).endVertex();
  }

  public static class CustomRenderType extends RenderType {

    public CustomRenderType(
        String pName,
        VertexFormat pFormat,
        VertexFormat.Mode pMode,
        int pBufferSize,
        boolean pAffectsCrumbling,
        boolean pSortOnUpload,
        Runnable pSetupState,
        Runnable pClearState) {
      super(
          pName,
          pFormat,
          pMode,
          pBufferSize,
          pAffectsCrumbling,
          pSortOnUpload,
          pSetupState,
          pClearState);
    }

    private static final RenderType RAINBOW_LINES =
        RenderType.create(
            "rainbow_lines_no_depth",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0D)))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(false));

    private static final RenderType RAINBOW_QUADS =
        RenderType.create(
            "rainbow_quads_no_depth",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(false));
  }
}

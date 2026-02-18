package com.ref.aea.api.client;

import com.ref.aea.util.BoxHelper;
import com.ref.aea.util.client.RenderUtil;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public interface IRainbowRender {
  IRainbowRender INSTANCE = new RenderUtil();

  Map<Direction, AABB> TOP_BOXES =
      BoxHelper.createRotatedBoxes(
          new AABB(2 / 16.0, 2 / 16.0, 14 / 16.0, 14 / 16.0, 14 / 16.0, 16 / 16.0).inflate(0.002D));

  Map<Direction, AABB> BOTTOM_BOXES =
      BoxHelper.createRotatedBoxes(
          new AABB(5 / 16.0, 5 / 16.0, 12 / 16.0, 11 / 16.0, 11 / 16.0, 14 / 16.0).inflate(0.002D));

  void drawRainbowBorder(
      GuiGraphics guiGraphics, int x, int y, int width, int height, float z, float bw);

  void drawWorldRainbowOutline(AABB box, RenderLevelStageEvent event);

  void drawWorldRainbowFill(AABB box, RenderLevelStageEvent event, float alpha);

  int getRainbowColor(long time, float offset);
}

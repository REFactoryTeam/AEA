package com.ref.aea.api.pos.client;

import com.ref.aea.api.client.IRainbowRender;
import com.ref.aea.api.pos.SidedGlobalPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface ISidedGlobalPosRender {
  @OnlyIn(Dist.CLIENT)
  default void renderGlobalPosInLevel(SidedGlobalPos pos, RenderLevelStageEvent event) {
    IRainbowRender.INSTANCE.drawWorldRainbowOutline(
        new AABB(pos.globalPos().pos()).inflate(0.002D), event);
  }
  ;

  @OnlyIn(Dist.CLIENT)
  default void renderDirectionInLevel(SidedGlobalPos pos, RenderLevelStageEvent event) {
    pos.direction()
        .ifPresent(
            direction -> {
              IRainbowRender.INSTANCE.drawWorldRainbowFill(
                  IRainbowRender.TOP_BOXES.get(direction).move(pos.globalPos().pos()), event, 0.5f);
              IRainbowRender.INSTANCE.drawWorldRainbowFill(
                  IRainbowRender.BOTTOM_BOXES.get(direction).move(pos.globalPos().pos()),
                  event,
                  0.3f);
            });
  }
  ;
}

package com.ref.aea.api.pos.client;

import com.ref.aea.api.client.ILevelRenderItem;
import com.ref.aea.api.pos.SidedGlobalPos;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

public interface ISidedGlobalPosRenderItem extends ILevelRenderItem, ISidedGlobalPosRender {
  @Override
  @OnlyIn(Dist.CLIENT)
  default void renderLevelOverlay(RenderLevelStageEvent event, ItemStack stack) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
      return;
    }
    Collection<SidedGlobalPos> sidedGlobalPos = this.getSidedGlobalPos(stack);
    sidedGlobalPos.forEach((pos) -> this.renderGlobalPosInLevel(pos, event));
    sidedGlobalPos.forEach((pos) -> this.renderDirectionInLevel(pos, event));
  }
  ;

  @NotNull
  @OnlyIn(Dist.CLIENT)
  Collection<SidedGlobalPos> getSidedGlobalPos(ItemStack stack);
}

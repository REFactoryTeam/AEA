package com.ref.aea.api.pos.client;

import com.ref.aea.api.client.ILevelRenderItem;
import com.ref.aea.api.pos.SidedGlobalPos;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

public interface ISidedGlobalPosRenderItem extends ILevelRenderItem, ISidedGlobalPosRender {

  @NotNull
  Collection<SidedGlobalPos> getSidedGlobalPos(@NotNull ItemStack stack);

  @Override
  @OnlyIn(Dist.CLIENT)
  default void renderLevelOverlay(@NotNull RenderLevelStageEvent event, @NotNull ItemStack stack) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
      return;
    }
    this.getSidedGlobalPos(stack).stream()
        .filter(SidedGlobalPos::isCurrentLevel)
        .forEach(
            pos ->
                getRenderActions().forEach(renderAction -> renderAction.render(pos, event, stack)));
  }

  default List<SidedGlobalPosRenderAction> getRenderActions() {
    return List.of(this::renderGlobalPosInLevel, this::renderDirectionInLevel);
  }

  @FunctionalInterface
  interface SidedGlobalPosRenderAction {
    void render(
        @NotNull SidedGlobalPos pos,
        @NotNull RenderLevelStageEvent event,
        @NotNull ItemStack stack);
  }
}

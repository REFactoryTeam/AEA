package com.ref.aea.api.wireless;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.ref.aea.api.client.IRainbowRender;
import com.ref.aea.api.pos.ISidedGlobalPosHost;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.pos.client.ISidedGlobalPosRenderItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IWirelessConnectionToolItem extends ISidedGlobalPosRenderItem {
  default void setLogic(@Nullable SidedGlobalPos pos, @NotNull ItemStack stack) {
    if (pos != null) {
      pos.toNbt(stack.getOrCreateTag());
    } else {
      SidedGlobalPos.removeNbt(stack.getTag());
    }
  }

  default Optional<IWirelessConnectionLogic> getLogic(@NotNull ItemStack stack) {
    return SidedGlobalPos.fromNbt(stack.getTag())
        .flatMap(
            pos -> {
              Level level =
                  ServerLifecycleHooks.getCurrentServer().getLevel(pos.globalPos().dimension());

              if (level != null) {
                BlockEntity be = level.getBlockEntity(pos.globalPos().pos());

                Direction direction = pos.direction().orElse(null);
                if (be instanceof IPartHost host) {
                  if (host.getPart(direction) instanceof IWirelessConnectionLogicHost logicHost) {
                    return Optional.of(logicHost.getLogic(direction));
                  }
                }
                if (be instanceof IWirelessConnectionLogicHost host) {
                  return Optional.of(host.getLogic(direction));
                }
              }
              return Optional.empty();
            });
  }

  default void addSidedGlobalPos(@NotNull SidedGlobalPos pos, @NotNull ItemStack stack) {
    this.getLogic(stack).ifPresent(logic -> logic.addSidedGlobalPos(pos));
  }

  default void removeSidedGlobalPos(@NotNull SidedGlobalPos pos, @NotNull ItemStack stack) {
    this.getLogic(stack).ifPresent(logic -> logic.removeSidedGlobalPos(pos));
  }

  default SidedGlobalPos getSidedGlobalPosFormUseOnContext(@NotNull UseOnContext context) {
    Level level = context.getLevel();
    BlockEntity be = level.getBlockEntity(context.getClickedPos());
    Direction targetSide = context.getClickedFace();

    if (be instanceof IPartHost host) {
      SelectedPart selectedPart = host.selectPartWorld(context.getClickLocation());
      if (selectedPart.side != null) {
        targetSide = selectedPart.side;
      }
    }
    return SidedGlobalPos.of(GlobalPos.of(level.dimension(), context.getClickedPos()), targetSide);
  }

  default void syncLogicToItem(@NotNull ItemStack stack) {
    this.getLogic(stack)
        .map(ISidedGlobalPosHost::getSidedGlobalPos)
        .filter(pos -> !pos.isEmpty())
        .ifPresentOrElse(
            positions -> SidedGlobalPos.listToNbt(stack.getOrCreateTag(), positions),
            () -> SidedGlobalPos.removeListNbt(stack.getTag()));
  }

  @OnlyIn(Dist.CLIENT)
  default void renderConnectionInLevel(
      @NotNull SidedGlobalPos pos, @NotNull RenderLevelStageEvent event, @NotNull ItemStack stack) {
    SidedGlobalPos.fromNbt(stack.getTag())
        .ifPresent(
            logicPos -> {
              if (!pos.globalPos().dimension().equals(logicPos.globalPos().dimension())) return;
              IRainbowRender.INSTANCE.drawWorldRainbowLine(logicPos.getVec(), pos.getVec(), event);
            });
  }

  @Override
  @NotNull
  default Collection<SidedGlobalPos> getSidedGlobalPos(@NotNull ItemStack stack) {
    List<SidedGlobalPos> list = new ArrayList<>(SidedGlobalPos.listFromNbt(stack.getTag()));
    SidedGlobalPos.fromNbt(stack.getTag()).ifPresent(list::add);
    return list;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  default void renderDirectionInLevel(
      @NotNull SidedGlobalPos pos, @NotNull RenderLevelStageEvent event, @NotNull ItemStack stack) {
    pos.direction()
        .ifPresent(
            direction ->
                IRainbowRender.INSTANCE.drawWorldRainbowFill(
                    IRainbowRender.FACE_CONNECTION_BOXES.get(direction).move(pos.globalPos().pos()),
                    event,
                    0.5f));
  }

  @Override
  default List<SidedGlobalPosRenderAction> getRenderActions() {
    List<SidedGlobalPosRenderAction> actions =
        new ArrayList<>(ISidedGlobalPosRenderItem.super.getRenderActions());
    actions.add(this::renderConnectionInLevel);
    return actions;
  }
}

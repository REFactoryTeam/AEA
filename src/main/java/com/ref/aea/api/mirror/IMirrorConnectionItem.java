package com.ref.aea.api.mirror;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.ref.aea.api.pos.SidedGlobalPos;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface IMirrorConnectionItem {

  default SidedGlobalPos getSidedGlobalPosFormUseOnContext(@NotNull UseOnContext context) {
    Level level = context.getLevel();
    BlockEntity be = level.getBlockEntity(context.getClickedPos());
    Direction targetSide = null;
    SidedGlobalPos sourceToRecord = null;

    if (be instanceof IPartHost host) {
      SelectedPart selectedPart = host.selectPartWorld(context.getClickLocation());
      if (selectedPart.side != null) {
        targetSide = selectedPart.side;
      }
      if (selectedPart.part instanceof IMirror<?> mirrorPart) {
        sourceToRecord = mirrorPart.getFirstSidedGlobalPos().orElse(null);
      }
    } else if (be instanceof IMirror<?> mirrorBlock) {
      sourceToRecord = mirrorBlock.getFirstSidedGlobalPos().orElse(null);
    }
    return sourceToRecord != null
        ? sourceToRecord
        : SidedGlobalPos.of(GlobalPos.of(level.dimension(), context.getClickedPos()), targetSide);
  }

  default Optional<SidedGlobalPos> getSidedGlobalPosFormItemStack(ItemStack itemStack) {
    return SidedGlobalPos.fromNbt(itemStack.getTag());
  }
}

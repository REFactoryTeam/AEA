package com.ref.aea.integration.ae2.mirror;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PushDirection;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import com.ref.aea.api.mirror.IMirror;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MirrorPatternProviderBlock<T extends MirrorPatternProviderBlockEntity>
    extends AEBaseEntityBlock<T> {
  public static final EnumProperty<PushDirection> PUSH_DIRECTION =
      EnumProperty.create("push_direction", PushDirection.class);

  public MirrorPatternProviderBlock() {
    super(metalProps());
    registerDefaultState(defaultBlockState().setValue(PUSH_DIRECTION, PushDirection.ALL));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(PUSH_DIRECTION);
  }

  @Override
  public void neighborChanged(
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Block block,
      @NotNull BlockPos fromPos,
      boolean isMoving) {
    var be = this.getBlockEntity(level, pos);
    if (be != null) {
      be.getLogic().updateRedstoneState();
    }
  }

  @Override
  public InteractionResult onActivated(
      Level level,
      BlockPos pos,
      Player p,
      InteractionHand hand,
      @Nullable ItemStack heldItem,
      BlockHitResult hit) {
    if (InteractionUtil.isInAlternateUseMode(p)) {
      return InteractionResult.PASS;
    }

    T be = this.getBlockEntity(level, pos);

    if (heldItem != null && !heldItem.isEmpty()) {
      if (InteractionUtil.canWrenchRotate(heldItem)) {
        this.setSide(level, pos, hit.getDirection());
        return InteractionResult.sidedSuccess(level.isClientSide);
      }
      CompoundTag tag = heldItem.getTag();
      if (be != null && tag != null) {
        Optional<InteractionResult> result =
            IMirror.readSourceFromNBT(heldItem.getTag())
                .map(
                    sourcePos -> {
                      if (!level.isClientSide) {
                        be.getLogic().setSourcePos(sourcePos);
                      }
                      return InteractionResult.sidedSuccess(level.isClientSide);
                    });
        if (result.isPresent()) return result.get();
      }
    }

    if (be == null) {
      return InteractionResult.PASS;
    }

    if (!level.isClientSide) {
      be.openMenu(p, MenuLocators.forBlockEntity(be));
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  public void setSide(Level level, BlockPos pos, Direction facing) {
    var currentState = level.getBlockState(pos);
    var pushSide = currentState.getValue(PUSH_DIRECTION).getDirection();

    PushDirection newPushDirection;
    if (pushSide == facing.getOpposite()) {
      newPushDirection = PushDirection.fromDirection(facing);
    } else if (pushSide == facing) {
      newPushDirection = PushDirection.ALL;
    } else if (pushSide == null) {
      newPushDirection = PushDirection.fromDirection(facing.getOpposite());
    } else {
      newPushDirection = PushDirection.fromDirection(Platform.rotateAround(pushSide, facing));
    }

    level.setBlockAndUpdate(pos, currentState.setValue(PUSH_DIRECTION, newPushDirection));
  }
}

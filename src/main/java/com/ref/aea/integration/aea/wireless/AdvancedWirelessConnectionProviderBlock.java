package com.ref.aea.integration.aea.wireless;

import appeng.block.AEBaseEntityBlock;
import appeng.util.InteractionUtil;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedWirelessConnectionProviderBlock
    extends AEBaseEntityBlock<AdvancedWirelessConnectionProviderBlockEntity> {

  public static final VoxelShape SHAPE = Shapes.box(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);

  public AdvancedWirelessConnectionProviderBlock() {
    super(metalProps().noOcclusion());
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

    if (heldItem != null
        && !heldItem.isEmpty()
        && heldItem.getItem() instanceof IWirelessConnectionToolItem wirelessConnectionToolItem) {
      wirelessConnectionToolItem.setLogic(
          SidedGlobalPos.of(GlobalPos.of(level.dimension(), pos), null), heldItem);
      return InteractionResult.sidedSuccess(!level.isClientSide);
    }
    return InteractionResult.PASS;
  }

  @Override
  public @NotNull VoxelShape getShape(
      @NotNull BlockState pState,
      @NotNull BlockGetter pLevel,
      @NotNull BlockPos pPos,
      @NotNull CollisionContext pContext) {
    return SHAPE;
  }
}

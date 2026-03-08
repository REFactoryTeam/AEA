package com.ref.aea.integration.aea.wireless;

import appeng.block.AEBaseEntityBlock;
import appeng.util.InteractionUtil;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionToolItem;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtendedWirelessConnectionProviderBlock
    extends AEBaseEntityBlock<ExtendedWirelessConnectionProviderBlockEntity> {

  public static final VoxelShape SHAPE =
      Stream.of(
              Shapes.box(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875),
              Shapes.box(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 0.125),
              Shapes.box(0.1875, 0.1875, 0.875, 0.8125, 0.8125, 1.0),
              Shapes.box(0.875, 0.1875, 0.1875, 1.0, 0.8125, 0.8125),
              Shapes.box(0.0, 0.1875, 0.1875, 0.125, 0.8125, 0.8125),
              Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 0.125, 0.8125),
              Shapes.box(0.1875, 0.875, 0.1875, 0.8125, 1.0, 0.8125))
          .reduce(Shapes::or)
          .get();

  private static final Map<AABB, Direction> TRIGGER_AREAS =
      Map.of(
          new AABB(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 0.125), Direction.NORTH,
          new AABB(0.1875, 0.1875, 0.875, 0.8125, 0.8125, 1.0), Direction.SOUTH,
          new AABB(0.875, 0.1875, 0.1875, 1.0, 0.8125, 0.8125), Direction.EAST,
          new AABB(0.0, 0.1875, 0.1875, 0.125, 0.8125, 0.8125), Direction.WEST,
          new AABB(0.1875, 0.0, 0.1875, 0.8125, 0.125, 0.8125), Direction.DOWN,
          new AABB(0.1875, 0.875, 0.1875, 0.8125, 1.0, 0.8125), Direction.UP);

  public ExtendedWirelessConnectionProviderBlock() {
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

      Vec3 hitVec = hit.getLocation();
      double relX = hitVec.x - pos.getX();
      double relY = hitVec.y - pos.getY();
      double relZ = hitVec.z - pos.getZ();
      Vec3 relativeHit = new Vec3(relX, relY, relZ);

      Direction clickedBoxSide = null;

      for (Map.Entry<AABB, Direction> entry : TRIGGER_AREAS.entrySet()) {
        if (entry.getKey().inflate(0.001).contains(relativeHit)) {
          clickedBoxSide = entry.getValue();
          break;
        }
      }
      if (clickedBoxSide != null) {
        wirelessConnectionToolItem.setLogic(
            SidedGlobalPos.of(GlobalPos.of(level.dimension(), pos), clickedBoxSide), heldItem);
        return InteractionResult.sidedSuccess(!level.isClientSide);
      }
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

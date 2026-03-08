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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WirelessConnectionProviderBlock
    extends AEBaseEntityBlock<WirelessConnectionProviderBlockEntity> {
  public WirelessConnectionProviderBlock() {
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
}

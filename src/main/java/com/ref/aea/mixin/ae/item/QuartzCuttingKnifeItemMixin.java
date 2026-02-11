package com.ref.aea.mixin.ae.item;

import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.parts.IPartHost;
import appeng.integration.modules.igtooltip.parts.PartHostTooltips;
import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import com.ref.aea.util.client.KeyboardHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(QuartzCuttingKnifeItem.class)
public class QuartzCuttingKnifeItemMixin extends Item {

  public QuartzCuttingKnifeItemMixin(Properties properties) {
    super(properties);
  }

  @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
  private void onUseOnClient(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
    Player player = context.getPlayer();
    Level level = context.getLevel();

    if (player == null || !player.isShiftKeyDown()) {
      return;
    }
    if (!level.isClientSide) {
      cir.setReturnValue(InteractionResult.SUCCESS);
      return;
    }
    BlockPos pos = context.getClickedPos();
    BlockState state = level.getBlockState(pos);
    BlockEntity tile = level.getBlockEntity(pos);
    Component blockNameComponent = null;
    if (tile instanceof IPartHost partHost) {
      blockNameComponent =
          PartHostTooltips.getName(
              partHost, new TooltipContext(null, context.getClickLocation(), player));
    }
    if (blockNameComponent == null && !state.isAir()) {
      blockNameComponent = state.getBlock().getName();
    }
    if (blockNameComponent != null) {
      KeyboardHandlerUtil.setClipboard(blockNameComponent);
      player.displayClientMessage(blockNameComponent, false);
    }
  }

  @Override
  public boolean overrideStackedOnOther(
      @NotNull ItemStack stack,
      @NotNull Slot slot,
      @NotNull ClickAction action,
      @NotNull Player player) {
    if (action == ClickAction.SECONDARY && slot.hasItem()) {
      if (player.isLocalPlayer()) {
        KeyboardHandlerUtil.setClipboard(slot.getItem().getHoverName());
        player.displayClientMessage(slot.getItem().getDisplayName(), false);
      }
      return true;
    }
    return false;
  }
}

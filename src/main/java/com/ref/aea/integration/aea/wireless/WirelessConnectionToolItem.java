package com.ref.aea.integration.aea.wireless;

import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionToolItem;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WirelessConnectionToolItem extends Item implements IWirelessConnectionToolItem {

  public WirelessConnectionToolItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack pStack,
      @Nullable Level pLevel,
      @NotNull List<Component> pTooltipComponents,
      @NotNull TooltipFlag pIsAdvanced) {
    SidedGlobalPos.fromNbt(pStack.getTag())
        .ifPresent(pos -> pTooltipComponents.add(pos.getToolTip()));
    if (getSidedGlobalPos(pStack).size() > 1) {
      pTooltipComponents.add(
          Component.translatable(
              AEAToolTips.WirelessConnectionAmount.getTranslationKey(),
              getSidedGlobalPos(pStack).size() - 1));
    }
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive()) {
      if (!level.isClientSide && SidedGlobalPos.removeNbt(stack.getTag())) {
        this.syncLogicToItem(stack);
        if (stack.getTag().isEmpty()) stack.setTag(null);
        player.displayClientMessage(
            Component.translatable(AEAToolTips.WirelessConnectionHostClear.getTranslationKey()),
            true);
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    return InteractionResultHolder.pass(stack);
  }

  @Override
  public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
    if (!context.isSecondaryUseActive()) {
      return InteractionResult.PASS;
    }
    Level level = context.getLevel();
    Player player = context.getPlayer();
    ItemStack stack = context.getItemInHand();
    SidedGlobalPos sourceToRecord = this.getSidedGlobalPosFormUseOnContext(context);

    SidedGlobalPos.fromNbt(stack.getTag())
        .ifPresent(
            pos -> {
              if (!pos.is(sourceToRecord) && !level.isClientSide) {
                this.addSidedGlobalPos(sourceToRecord, stack);
                this.syncLogicToItem(stack);
                if (player != null) {
                  player.displayClientMessage(sourceToRecord.getToolTip(), true);
                }
              }
            });
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void inventoryTick(
      @NotNull ItemStack stack,
      Level level,
      @NotNull Entity entity,
      int slotId,
      boolean isSelected) {
    if (!level.isClientSide && entity.tickCount % 5 == 0) {
      this.syncLogicToItem(stack);
    }
  }
}

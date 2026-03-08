package com.ref.aea.integration.aea.mirror;

import com.ref.aea.api.mirror.IMirrorConnectionItem;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.pos.client.ISidedGlobalPosRenderItem;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MirrorConnectionToolItem extends Item
    implements ISidedGlobalPosRenderItem, IMirrorConnectionItem {

  public MirrorConnectionToolItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack pStack,
      @Nullable Level pLevel,
      @NotNull List<Component> pTooltipComponents,
      @NotNull TooltipFlag pIsAdvanced) {
    SidedGlobalPos.fromNbt(pStack.getTag())
        .ifPresent((sourcePos) -> pTooltipComponents.add(sourcePos.getToolTip()));
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive()) {
      if (!level.isClientSide && SidedGlobalPos.removeNbt(stack.getTag())) {
        if (stack.getTag().isEmpty()) stack.setTag(null);
        player.displayClientMessage(
            Component.translatable(AEAToolTips.MirrorInfoClear.getTranslationKey()), true);
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
    SidedGlobalPos sourceToRecord = this.getSidedGlobalPosFormUseOnContext(context);

    if (!level.isClientSide) {
      sourceToRecord.toNbt(context.getItemInHand().getOrCreateTag());
      if (player != null) {
        player.displayClientMessage(sourceToRecord.getToolTip(), true);
      }
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public @NotNull Collection<SidedGlobalPos> getSidedGlobalPos(@NotNull ItemStack stack) {
    return SidedGlobalPos.fromNbt(stack.getTag()).map(List::of).orElse(List.of());
  }
}

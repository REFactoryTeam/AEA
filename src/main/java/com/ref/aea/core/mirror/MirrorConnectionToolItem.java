package com.ref.aea.core.mirror;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.ref.aea.api.client.ILevelRenderItem;
import com.ref.aea.api.client.IRainbowRender;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MirrorConnectionToolItem extends Item implements ILevelRenderItem {

  public MirrorConnectionToolItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack pStack,
      @Nullable Level pLevel,
      @NotNull List<Component> pTooltipComponents,
      @NotNull TooltipFlag pIsAdvanced) {
    var tag = pStack.getTag();
    if (tag == null) return;
    IMirror.readSourceFromNBT(tag)
        .ifPresent((sourcePos) -> pTooltipComponents.add(IMirror.getToolTip(sourcePos)));
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      @NotNull Level level, Player player, @NotNull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive()) {
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.contains(IMirror.NBT_SOURCE_POS)) {
        if (!level.isClientSide) {
          tag.remove(IMirror.NBT_SOURCE_POS);
          player.displayClientMessage(
              Component.translatable(AEAToolTips.MirrorInfoClear.getTranslationKey()), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
      }
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
    BlockEntity be = level.getBlockEntity(context.getClickedPos());
    Direction targetSide = context.getClickedFace();

    IMirror.SourcePos sourceToRecord = null;

    if (be instanceof IPartHost host) {
      SelectedPart selectedPart = host.selectPartWorld(context.getClickLocation());
      if (selectedPart.side != null) {
        targetSide = selectedPart.side;
      }
      if (selectedPart.part instanceof IMirror<?> mirrorPart) {
        sourceToRecord = mirrorPart.getSourcePos();
      }
    } else if (be instanceof IMirror<?> mirrorBlock) {
      sourceToRecord = mirrorBlock.getSourcePos();
    }

    if (sourceToRecord == null) {
      sourceToRecord =
          new IMirror.SourcePos(
              GlobalPos.of(level.dimension(), context.getClickedPos()), targetSide);
    }
    if (!level.isClientSide) {
      IMirror.writeSourceToNBT(context.getItemInHand().getOrCreateTag(), sourceToRecord);
      if (player != null) {
        player.displayClientMessage(IMirror.getToolTip(sourceToRecord), true);
      }
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void renderLevelOverlay(RenderLevelStageEvent event, ItemStack stack) {
    if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || stack.getTag() == null) {
      return;
    }
    IMirror.readSourceFromNBT(stack.getTag())
        .ifPresent(
            sourcePos -> {
              ClientLevel clientLevel = Minecraft.getInstance().level;
              if (clientLevel == null
                  || !clientLevel.dimension().equals(sourcePos.globalPos().dimension())) {
                return;
              }
              IRainbowRender.INSTANCE.drawWorldRainbowOutline(
                  new AABB(sourcePos.globalPos().pos()).inflate(0.002D), event);
              if (Screen.hasShiftDown()) {
                IRainbowRender.INSTANCE.drawWorldRainbowFill(
                    IRainbowRender.TOP_BOXES
                        .get(sourcePos.direction())
                        .move(sourcePos.globalPos().pos()),
                    event,
                    0.5f);
                IRainbowRender.INSTANCE.drawWorldRainbowFill(
                    IRainbowRender.BOTTOM_BOXES
                        .get(sourcePos.direction())
                        .move(sourcePos.globalPos().pos()),
                    event,
                    0.3f);
              }
            });
  }
}

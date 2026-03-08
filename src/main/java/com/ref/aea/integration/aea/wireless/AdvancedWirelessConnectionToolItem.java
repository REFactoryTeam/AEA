package com.ref.aea.integration.aea.wireless;

import com.ref.aea.api.common.ICustomScrollBehavior;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedWirelessConnectionToolItem extends WirelessConnectionToolItem
    implements ICustomScrollBehavior {

  @Override
  public void appendHoverText(
      @NotNull ItemStack pStack,
      @Nullable Level pLevel,
      @NotNull List<Component> pTooltipComponents,
      @NotNull TooltipFlag pIsAdvanced) {
    super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    if (pStack.getTag() == null) return;
    pTooltipComponents.add(
        Component.translatable(
            AEAToolTips.WirelessConnectionFrequency.getTranslationKey(), getFrequency(pStack)));
    List<Integer> activeFreqs = getActiveFrequencies(pStack);
    if (!activeFreqs.isEmpty()) {
      String freqsString =
          activeFreqs.stream().map(String::valueOf).collect(Collectors.joining(", "));
      pTooltipComponents.add(Component.literal("(" + freqsString + ")"));
    }
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive()) {
      if (!level.isClientSide && SidedGlobalPos.removeNbt(stack.getTag())) {
        this.setFrequency(stack, 0);
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
              if (!level.isClientSide)
                if (!pos.is(sourceToRecord)) {
                  this.addSidedGlobalPos(sourceToRecord, stack);
                  this.syncLogicToItem(stack);
                  if (player != null) {
                    player.displayClientMessage(sourceToRecord.getToolTip(), true);
                  }
                } else {
                  int frequency = this.getFrequency(stack) + 1;
                  this.setFrequency(stack, frequency);
                  if (player != null) {
                    player.displayClientMessage(
                        Component.translatable(
                            AEAToolTips.WirelessConnectionFrequency.getTranslationKey(), frequency),
                        true);
                  }
                }
            });
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void syncLogicToItem(@NotNull ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag == null) return;
    if (tag.isEmpty()) {
      stack.setTag(null);
      return;
    }
    this.getLogic(stack)
        .ifPresentOrElse(
            logic -> {
              Collection<SidedGlobalPos> positions;
              if (logic instanceof AdvancedWirelessConnectionLogic advanced) {
                positions = advanced.getSidedGlobalPos(getFrequency(stack));
                List<Integer> freqs = advanced.getFrequencys();
                if (!freqs.isEmpty()) {
                  stack.getOrCreateTag().putIntArray("active_frequencies", freqs);
                } else {
                  tag.remove("active_frequencies");
                }
              } else {
                positions = logic.getSidedGlobalPos();
              }
              if (!positions.isEmpty()) {
                SidedGlobalPos.listToNbt(stack.getOrCreateTag(), positions);
              } else {
                SidedGlobalPos.removeListNbt(tag);
              }
            },
            () -> {
              SidedGlobalPos.removeListNbt(tag);
              tag.remove("active_frequencies");
            });
  }

  public List<Integer> getActiveFrequencies(@NotNull ItemStack stack) {
    if (stack.getTag() != null) {
      int[] array = stack.getTag().getIntArray("active_frequencies");
      return Arrays.stream(array).boxed().toList();
    }
    return List.of();
  }

  @Override
  public void addSidedGlobalPos(@NotNull SidedGlobalPos pos, @NotNull ItemStack stack) {
    this.getLogic(stack)
        .ifPresent(
            logic -> {
              if (logic
                  instanceof AdvancedWirelessConnectionLogic advancedWirelessConnectionLogic) {
                advancedWirelessConnectionLogic.addSidedGlobalPos(pos, getFrequency(stack));
              } else {
                logic.addSidedGlobalPos(pos);
              }
            });
  }

  public int getFrequency(@NotNull ItemStack stack) {
    if (stack.getTag() != null) {
      return stack.getTag().getInt("frequency");
    } else {
      return 0;
    }
  }

  public void setFrequency(@NotNull ItemStack stack, int frequency) {
    if (frequency == 0) {
      if (stack.getTag() != null) {
        stack.getTag().remove("frequency");
      }
    } else {
      stack.getOrCreateTag().putInt("frequency", frequency);
    }
  }

  @Override
  public void onScroll(@NotNull ItemStack stack, @NotNull Player player, double delta) {
    if (!player.level().isClientSide) {
      List<Integer> activeFreqs = getActiveFrequencies(stack);
      if (activeFreqs.isEmpty()) {
        return;
      }
      int currentFreq = getFrequency(stack);
      int currentIndex = activeFreqs.indexOf(currentFreq);
      int newFreq;
      if (currentIndex == -1) {
        newFreq = activeFreqs.get(0);
      } else {
        int size = activeFreqs.size();
        int newIndex;
        if (delta > 0) {
          newIndex = (currentIndex + 1) % size;
        } else {
          newIndex = (currentIndex - 1 + size) % size;
        }
        newFreq = activeFreqs.get(newIndex);
      }

      setFrequency(stack, newFreq);
      this.syncLogicToItem(stack);

      player.displayClientMessage(
          Component.translatable(
              AEAToolTips.WirelessConnectionFrequency.getTranslationKey(), newFreq),
          true);
    }
  }
}

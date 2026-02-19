package com.ref.aea.integration.ftbultimine;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.items.tools.MemoryCardItem;
import appeng.util.SettingsFrom;
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AEMemoryCardHandler {

  private static final Direction[] ALL_SIDES = {
    Direction.DOWN,
    Direction.UP,
    Direction.NORTH,
    Direction.SOUTH,
    Direction.WEST,
    Direction.EAST,
    null
  };

  public static int applySettings(
      ServerPlayer player,
      InteractionHand hand,
      BlockPos clickPos,
      Direction face,
      FTBUltiminePlayerData data) {
    ItemStack stack = player.getItemInHand(hand);
    if (!(stack.getItem() instanceof IMemoryCard memoryCard)) return 0;

    String storedName = memoryCard.getSettingsName(stack);
    CompoundTag settingsData = memoryCard.getData(stack);

    if (storedName == null
        || storedName.isEmpty()
        || settingsData == null
        || settingsData.isEmpty()) {
      return 0;
    }

    int appliedCount = 0;
    Level level = player.level();

    for (BlockPos pos : data.cachedPositions()) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be == null) continue;

      boolean blockSuccess = false;

      if (be instanceof IPartHost host) {
        for (Direction side : ALL_SIDES) {
          IPart part = host.getPart(side);
          if (part != null) {
            if (performApply(part, getSettingsId(part), storedName, settingsData, player)) {
              blockSuccess = true;
            }
          }
        }
      } else if (be instanceof AEBaseBlockEntity aeBe) {
        if (performApply(aeBe, getSettingsId(aeBe), storedName, settingsData, player)) {
          blockSuccess = true;
        }
      }

      if (blockSuccess) {
        appliedCount++;
      }
    }
    if (appliedCount > 0) {
      memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
    }

    return appliedCount;
  }

  private static String getSettingsId(Object target) {
    if (target instanceof IPart part) {
      Item partItem = part.getPartItem().asItem();
      if (AEParts.INTERFACE.asItem() == partItem) {
        partItem = AEBlocks.INTERFACE.asItem();
      } else if (AEParts.PATTERN_PROVIDER.asItem() == partItem) {
        partItem = AEBlocks.PATTERN_PROVIDER.asItem();
      }
      return partItem.getDescriptionId();
    } else if (target instanceof AEBaseBlockEntity be) {
      return be.getBlockState().getBlock().getDescriptionId();
    }
    return "";
  }

  private static boolean performApply(
      Object target, String targetName, String storedName, CompoundTag data, ServerPlayer player) {
    try {
      if (Objects.equals(targetName, storedName)) {
        if (target instanceof IPart part) {
          part.importSettings(SettingsFrom.MEMORY_CARD, data, player);
        } else if (target instanceof AEBaseBlockEntity be) {
          be.importSettings(SettingsFrom.MEMORY_CARD, data, player);
        }
      } else {
        if (target instanceof IPart part) {
          MemoryCardItem.importGenericSettings(part, data, player);
        } else if (target instanceof AEBaseBlockEntity be) {
          MemoryCardItem.importGenericSettings(be, data, player);
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

package com.ref.aea.integration.jade;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.ref.aea.AEA;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionLogicHost;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum WirelessConnectionProvider
    implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  public final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "wireless_connection_provider");

  @Override
  public void appendTooltip(
      ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
    CompoundTag serverData = blockAccessor.getServerData();

    Direction direction = blockAccessor.getSide();

    BlockEntity be = blockAccessor.getBlockEntity();
    if (be instanceof IPartHost partHost) {
      SelectedPart selectedPart =
          partHost.selectPartWorld(blockAccessor.getHitResult().getLocation());
      if (selectedPart.part instanceof IWirelessConnectionLogicHost) {
        if (selectedPart.side != null) {
          direction = selectedPart.side;
        }
      } else {
        return;
      }
    } else if (!(be instanceof IWirelessConnectionLogicHost)) {
      return;
    }
    CompoundTag tag = serverData.getCompound(direction.getName());
    List<SidedGlobalPos> positions = SidedGlobalPos.listFromNbt(tag);
    if (positions.isEmpty()) {
      return;
    }
    Map<ResourceLocation, Long> dimensionCounts =
        positions.stream()
            .collect(
                Collectors.groupingBy(
                    pos -> pos.globalPos().dimension().location(), Collectors.counting()));

    dimensionCounts.entrySet().stream()
        .sorted(Map.Entry.<ResourceLocation, Long>comparingByValue().reversed())
        .forEach(
            entry ->
                iTooltip.add(
                    Component.literal(entry.getKey().toString())
                        .append(Component.literal(": "))
                        .append(
                            Component.translatable(
                                AEAToolTips.WirelessConnectionAmount.getTranslationKey(),
                                entry.getValue()))));
  }

  @Override
  public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
    BlockEntity be = blockAccessor.getBlockEntity();
    Map<Direction, Collection<SidedGlobalPos>> pos = new HashMap<>();
    if (be instanceof IPartHost partHost) {
      for (Direction direction : Direction.values()) {
        if (partHost.getPart(direction) instanceof IWirelessConnectionLogicHost host) {
          pos.put(direction, host.getLogic(direction).getSidedGlobalPos());
        }
      }

    } else if (be instanceof IWirelessConnectionLogicHost host) {
      for (Direction direction : Direction.values()) {
        pos.put(direction, host.getLogic(direction).getSidedGlobalPos());
      }
    }
    pos.forEach(
        (direction, sidedGlobalPos) ->
            compoundTag.put(direction.getName(), SidedGlobalPos.listToNbt(sidedGlobalPos)));
  }

  @Override
  public ResourceLocation getUid() {
    return ID;
  }
}

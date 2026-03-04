package com.ref.aea.integration.jade;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import com.ref.aea.AEA;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.api.pos.SidedGlobalPos;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public enum MirrorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  public final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "mirror_provider");

  private static final String KEY_MIRROR_DATA = "MirrorData";
  private static final String KEY_SIDE_BLOCK = "block";
  private static final String KEY_SIDE_CENTER = "center";

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    CompoundTag serverData = accessor.getServerData();
    if (!serverData.contains(KEY_MIRROR_DATA, Tag.TAG_COMPOUND)) return;
    String targetKey = getTargetKey(accessor);
    if (targetKey == null) return;
    CompoundTag mirrorsMap = serverData.getCompound(KEY_MIRROR_DATA);
    if (mirrorsMap.contains(targetKey, Tag.TAG_COMPOUND)) {
      CompoundTag sourceTag = mirrorsMap.getCompound(targetKey);
      SidedGlobalPos.fromNbt(sourceTag).ifPresent(sourcePos -> tooltip.add(sourcePos.getToolTip()));
    }
  }

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    BlockEntity be = accessor.getBlockEntity();
    CompoundTag mirrorsMap = new CompoundTag();
    if (be instanceof IPartHost partHost) {
      for (Direction direction : Direction.values()) {
        if (partHost.getPart(direction) instanceof IMirror<?> mirror) {
          writeMirrorToMap(mirrorsMap, mirror, direction.getName());
        }
        if (partHost.getPart(null) instanceof IMirror<?> mirror) {
          writeMirrorToMap(mirrorsMap, mirror, KEY_SIDE_CENTER);
        }
      }
    }
    if (be instanceof IMirror<?> mirror) {
      writeMirrorToMap(mirrorsMap, mirror, KEY_SIDE_BLOCK);
    }
    if (!mirrorsMap.isEmpty()) {
      data.put(KEY_MIRROR_DATA, mirrorsMap);
    }
  }

  private void writeMirrorToMap(CompoundTag map, IMirror<?> mirror, String key) {
    mirror
        .getFirstSidedGlobalPos()
        .ifPresent(sidedGlobalPos -> map.put(key, sidedGlobalPos.toNbt()));
  }

  @Nullable
  private String getTargetKey(BlockAccessor accessor) {
    BlockEntity be = accessor.getBlockEntity();
    if (be instanceof IPartHost partHost) {
      SelectedPart selectedPart = partHost.selectPartWorld(accessor.getHitResult().getLocation());
      if (selectedPart != null && selectedPart.part instanceof IMirror<?>) {
        Direction side = selectedPart.side;
        return side == null ? KEY_SIDE_CENTER : side.getName();
      }
    } else if (be instanceof IMirror<?>) {
      return KEY_SIDE_BLOCK;
    }
    return null;
  }

  @Override
  public ResourceLocation getUid() {
    return this.ID;
  }
}

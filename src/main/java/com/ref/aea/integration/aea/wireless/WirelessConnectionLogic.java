package com.ref.aea.integration.aea.wireless;

import appeng.api.networking.*;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionLogic;
import com.ref.aea.api.wireless.IWirelessConnectionLogicHost;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WirelessConnectionLogic implements IGridTickable, IWirelessConnectionLogic {

  private final IWirelessConnectionLogicHost host;
  private final IManagedGridNode mainNode;

  private final Set<SidedGlobalPos> targetPositions = new HashSet<>();

  private final Map<SidedGlobalPos, IGridConnection> activeConnections = new HashMap<>();

  public WirelessConnectionLogic(IManagedGridNode mainNode, IWirelessConnectionLogicHost host) {
    this.mainNode =
        mainNode.setFlags(GridFlags.DENSE_CAPACITY).addService(IGridTickable.class, this);
    this.host = host;
  }

  @Override
  public TickingRequest getTickingRequest(IGridNode node) {
    return new TickingRequest(TickRates.METunnel, false, false);
  }

  @Override
  public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
    if (!mainNode.isReady()) {
      return TickRateModulation.IDLE;
    }

    validateAndMaintainConnections();

    return (activeConnections.size() >= targetPositions.size() && !targetPositions.isEmpty())
        ? TickRateModulation.SLOWER
        : TickRateModulation.SAME;
  }

  private void validateAndMaintainConnections() {
    if (!mainNode.isReady()) return;
    IGridNode myNode = mainNode.getNode();
    if (myNode == null) return;

    Level level = host.getBlockEntity().getLevel();
    if (!(level instanceof ServerLevel serverLevel)) return;
    MinecraftServer server = serverLevel.getServer();

    Iterator<Map.Entry<SidedGlobalPos, IGridConnection>> it =
        activeConnections.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<SidedGlobalPos, IGridConnection> entry = it.next();
      SidedGlobalPos target = entry.getKey();
      IGridConnection conn = entry.getValue();

      boolean shouldDisconnect = false;

      if (!targetPositions.contains(target)) {
        shouldDisconnect = true;
      } else if (!myNode.getConnections().contains(conn)) {
        it.remove();
        continue;
      } else {
        ServerLevel targetLevel = server.getLevel(target.globalPos().dimension());
        if (targetLevel == null || !targetLevel.isLoaded(target.globalPos().pos())) {
          shouldDisconnect = true;
        }
      }

      if (shouldDisconnect) {
        conn.destroy();
        it.remove();
      }
    }

    for (SidedGlobalPos target : targetPositions) {
      if (activeConnections.containsKey(target)) continue;

      ServerLevel targetLevel = server.getLevel(target.globalPos().dimension());
      if (targetLevel == null) continue;

      if (!targetLevel.isLoaded(target.globalPos().pos())) continue;

      IGridNode remoteNode =
          GridHelper.getExposedNode(
              targetLevel, target.globalPos().pos(), target.direction().orElse(null));

      if (remoteNode == null || remoteNode == myNode) continue;

      try {
        IGridConnection connection = GridHelper.createConnection(myNode, remoteNode);
        activeConnections.put(target, connection);
      } catch (Exception ignored) {

      }
    }
  }

  @Override
  public void destroy() {
    for (IGridConnection conn : activeConnections.values()) {
      conn.destroy();
    }
    activeConnections.clear();
  }

  public void writeToNBT(CompoundTag tag) {
    SidedGlobalPos.listToNbt(tag, targetPositions);
  }

  public void readFromNBT(CompoundTag tag) {
    destroy();
    targetPositions.clear();
    targetPositions.addAll(SidedGlobalPos.listFromNbt(tag));
  }

  @Override
  public @NotNull Collection<SidedGlobalPos> getSidedGlobalPos() {
    return this.targetPositions;
  }

  @Override
  public void addSidedGlobalPos(@NotNull SidedGlobalPos pos) {
    if (targetPositions.add(pos)) {
      host.saveChanges();
      validateAndMaintainConnections();
    } else {
      removeSidedGlobalPos(pos);
    }
  }

  @Override
  public void removeSidedGlobalPos(@NotNull SidedGlobalPos pos) {
    if (targetPositions.remove(pos)) {
      IGridConnection conn = activeConnections.remove(pos);
      if (conn != null) {
        conn.destroy();
      }
      host.saveChanges();
    }
  }

  @Override
  public void clearSidedGlobalPos() {
    if (!targetPositions.isEmpty()) {
      destroy();
      targetPositions.clear();
      host.saveChanges();
    }
  }
}

package com.ref.aea.integration.aea.wireless;

import appeng.api.networking.*;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.api.wireless.IWirelessConnectionLogic;
import com.ref.aea.api.wireless.IWirelessConnectionLogicHost;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AdvancedWirelessConnectionLogic implements IGridTickable, IWirelessConnectionLogic {

  private final IWirelessConnectionLogicHost host;
  private final IManagedGridNode mainNode;

  private final Map<SidedGlobalPos, Integer> targetPositions = new HashMap<>();
  private final Map<Integer, IManagedGridNode> subNodes = new HashMap<>();
  private final Map<SidedGlobalPos, IGridConnection> activeConnections = new HashMap<>();

  public AdvancedWirelessConnectionLogic(
      IManagedGridNode mainNode, IWirelessConnectionLogicHost host) {
    this.host = host;
    this.mainNode =
        mainNode
            .setFlags(GridFlags.DENSE_CAPACITY)
            .addService(IGridTickable.class, this)
            .setExposedOnSides(EnumSet.noneOf(Direction.class));
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
    Level level = host.getBlockEntity().getLevel();
    if (!(level instanceof ServerLevel serverLevel)) return;

    MinecraftServer server = serverLevel.getServer();

    cleanUpInvalidConnections(server);

    for (Map.Entry<SidedGlobalPos, Integer> entry : targetPositions.entrySet()) {
      SidedGlobalPos target = entry.getKey();
      int frequency = entry.getValue();

      if (activeConnections.containsKey(target)) continue;

      ServerLevel targetLevel = server.getLevel(target.globalPos().dimension());
      if (targetLevel == null || !targetLevel.isLoaded(target.globalPos().pos())) continue;

      IGridNode remoteNode =
          GridHelper.getExposedNode(
              targetLevel, target.globalPos().pos(), target.direction().orElse(null));

      if (remoteNode == null || remoteNode == mainNode.getNode()) continue;

      establishConnection(target, remoteNode, frequency);
    }
  }

  private void establishConnection(SidedGlobalPos target, IGridNode remoteNode, int frequency) {
    IManagedGridNode subNode = subNodes.computeIfAbsent(frequency, this::createNewSubNode);
    IGridNode nodeNode = subNode.getNode();
    if (nodeNode == null) return;
    try {
      IGridConnection conn = GridHelper.createConnection(nodeNode, remoteNode);
      activeConnections.put(target, conn);
    } catch (Exception ignored) {
    }
  }

  private IManagedGridNode createNewSubNode(int frequency) {
    IManagedGridNode newNode =
        GridHelper.createManagedNode(
                (IGridConnectedBlockEntity) host.getBlockEntity(), BlockEntityNodeListener.INSTANCE)
            .setTagName("wireless_sub_freq_" + frequency)
            .setInWorldNode(true)
            .setFlags(GridFlags.DENSE_CAPACITY)
            .setIdlePowerUsage(0)
            .setExposedOnSides(EnumSet.noneOf(Direction.class));

    newNode.create(host.getBlockEntity().getLevel(), host.getBlockEntity().getBlockPos());

    IGridNode mainGridNode = mainNode.getNode();
    IGridNode subGridNode = newNode.getNode();
    if (mainGridNode != null && subGridNode != null) {
      try {
        GridHelper.createConnection(mainGridNode, subGridNode);
      } catch (Exception ignored) {
      }
    }
    return newNode;
  }

  private void cleanUpInvalidConnections(MinecraftServer server) {
    Iterator<Map.Entry<SidedGlobalPos, IGridConnection>> it =
        activeConnections.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<SidedGlobalPos, IGridConnection> entry = it.next();
      SidedGlobalPos target = entry.getKey();
      IGridConnection conn = entry.getValue();

      if (shouldDisconnect(server, target, conn)) {
        conn.destroy();
        it.remove();
      }
    }

    Set<Integer> usedFrequencies = new HashSet<>(targetPositions.values());

    Iterator<Map.Entry<Integer, IManagedGridNode>> nodeIt = subNodes.entrySet().iterator();
    while (nodeIt.hasNext()) {
      Map.Entry<Integer, IManagedGridNode> entry = nodeIt.next();
      if (!usedFrequencies.contains(entry.getKey())) {
        entry.getValue().destroy();
        nodeIt.remove();
      }
    }
  }

  private boolean shouldDisconnect(
      MinecraftServer server, SidedGlobalPos target, IGridConnection conn) {
    if (!targetPositions.containsKey(target)) return true;

    ServerLevel targetLevel = server.getLevel(target.globalPos().dimension());
    if (targetLevel == null || !targetLevel.isLoaded(target.globalPos().pos())) return true;

    int currentFreq = targetPositions.get(target);
    IManagedGridNode correctSubNode = subNodes.get(currentFreq);

    if (correctSubNode == null || correctSubNode.getNode() == null) return true;

    return !conn.a().equals(correctSubNode.getNode()) && !conn.b().equals(correctSubNode.getNode());
  }

  @Override
  public void destroy() {
    activeConnections.values().forEach(IGridConnection::destroy);
    activeConnections.clear();
    subNodes.values().forEach(IManagedGridNode::destroy);
    subNodes.clear();
  }

  public void writeToNBT(CompoundTag tag) {
    Map<Integer, List<SidedGlobalPos>> groupedPositions = new HashMap<>();
    targetPositions.forEach(
        (pos, freq) -> groupedPositions.computeIfAbsent(freq, k -> new ArrayList<>()).add(pos));

    ListTag freqListTag = new ListTag();

    for (Map.Entry<Integer, List<SidedGlobalPos>> entry : groupedPositions.entrySet()) {
      CompoundTag freqGroupTag = new CompoundTag();

      freqGroupTag.putInt("Freq", entry.getKey());

      SidedGlobalPos.listToNbt(freqGroupTag, entry.getValue());

      freqListTag.add(freqGroupTag);
    }

    tag.put("FrequencyGroups", freqListTag);
  }

  public void readFromNBT(CompoundTag tag) {
    destroy();
    targetPositions.clear();
    if (tag.contains("FrequencyGroups", Tag.TAG_LIST)) {
      ListTag freqListTag = tag.getList("FrequencyGroups", Tag.TAG_COMPOUND);

      for (int i = 0; i < freqListTag.size(); i++) {
        CompoundTag freqGroupTag = freqListTag.getCompound(i);

        int freq = freqGroupTag.getInt("Freq");

        List<SidedGlobalPos> positions = SidedGlobalPos.listFromNbt(freqGroupTag);

        for (SidedGlobalPos pos : positions) {
          targetPositions.put(pos, freq);
        }
      }
    }
  }

  @Override
  public @NotNull Collection<SidedGlobalPos> getSidedGlobalPos() {
    return this.targetPositions.keySet();
  }

  public @NotNull Collection<SidedGlobalPos> getSidedGlobalPos(int frequency) {
    return targetPositions.entrySet().stream()
        .filter(entry -> entry.getValue() == frequency)
        .map(Map.Entry::getKey)
        .toList();
  }

  @Override
  public void addSidedGlobalPos(@NotNull SidedGlobalPos pos) {
    this.addSidedGlobalPos(pos, 0);
  }

  public void addSidedGlobalPos(@NotNull SidedGlobalPos pos, int frequency) {
    Integer existingFrequency = targetPositions.get(pos);

    if (existingFrequency != null) {
      if (existingFrequency == frequency) {
        removeSidedGlobalPos(pos);
        return;
      } else {
        IGridConnection conn = activeConnections.remove(pos);
        if (conn != null) {
          conn.destroy();
        }
        targetPositions.put(pos, frequency);
      }
    } else {
      targetPositions.put(pos, frequency);
    }

    host.saveChanges();
    validateAndMaintainConnections();
  }

  @Override
  public void removeSidedGlobalPos(@NotNull SidedGlobalPos pos) {
    if (targetPositions.remove(pos) != null) {
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

  public List<Integer> getFrequencys() {
    return this.targetPositions.values().stream().distinct().sorted().toList();
  }
}

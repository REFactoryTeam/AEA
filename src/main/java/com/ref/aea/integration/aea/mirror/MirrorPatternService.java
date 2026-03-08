package com.ref.aea.integration.aea.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.parts.AEBasePart;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.api.pos.SidedGlobalPos;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MirrorPatternService implements IMirrorPatternService, IGridServiceProvider {

  private final Map<SidedGlobalPos, Set<IGridNode>> mirrorNodeMap = new ConcurrentHashMap<>();
  private final Map<IGridNode, SidedGlobalPos> mirrorReverseMap = new ConcurrentHashMap<>();
  private final Map<SidedGlobalPos, IGridNode> sourceNodeMap = new ConcurrentHashMap<>();

  private final Set<IGridNode> nodesToUpdate = ConcurrentHashMap.newKeySet();

  public MirrorPatternService() {}

  @Override
  public void onLevelEndTick(Level level) {
    if (nodesToUpdate.isEmpty()) return;

    Iterator<IGridNode> iterator = nodesToUpdate.iterator();
    while (iterator.hasNext()) {
      IGridNode node = iterator.next();
      iterator.remove();

      if (node == null || !node.isActive() || node.getGrid() == null) {
        continue;
      }

      IMirror<?> mirror = node.getService(IMirror.class);
      if (mirror == null) {
        continue;
      }

      var optPos = mirror.getFirstSidedGlobalPos();
      if (optPos.isEmpty()) {
        mirror.updateSource(null);
        continue;
      }

      SidedGlobalPos srcPos = optPos.get();
      IGridNode targetNode = sourceNodeMap.get(srcPos);

      if (targetNode == null && srcPos.direction().isPresent()) {
        targetNode = sourceNodeMap.get(SidedGlobalPos.of(srcPos.globalPos(), null));
      }

      mirror.updateSource(targetNode);
    }
  }

  @Override
  public void refreshNode(IGridNode node) {
    SidedGlobalPos sourcePos = getSourcePosFromNode(node);
    if (sourcePos == null) return;

    addMirrorsToUpdateQueue(sourcePos);

    if (sourcePos.direction().isEmpty()) {
      GlobalPos gPos = sourcePos.globalPos();
      for (Direction direction : Direction.values()) {
        addMirrorsToUpdateQueue(SidedGlobalPos.of(gPos, direction));
      }
    }
  }

  private void addMirrorsToUpdateQueue(SidedGlobalPos pos) {
    Set<IGridNode> mirrors = mirrorNodeMap.get(pos);
    if (mirrors != null && !mirrors.isEmpty()) {
      nodesToUpdate.addAll(mirrors);
    }
  }

  public void updateMirrorPosition(IGridNode mirrorNode, SidedGlobalPos newPos) {
    removeOldMirrorNode(mirrorNode);
    if (newPos != null) {
      mirrorReverseMap.put(mirrorNode, newPos);
      mirrorNodeMap.computeIfAbsent(newPos, k -> ConcurrentHashMap.newKeySet()).add(mirrorNode);
      nodesToUpdate.add(mirrorNode);
    }
  }

  @Override
  public void addNode(IGridNode gridNode, CompoundTag savedData) {
    IMirror<?> mirror = gridNode.getService(IMirror.class);
    if (mirror != null) {
      mirror
          .getFirstSidedGlobalPos()
          .ifPresent(
              pos -> {
                updateMirrorPosition(gridNode, pos);
                nodesToUpdate.add(gridNode);
              });
    } else {
      SidedGlobalPos pos = getSourcePosFromNode(gridNode);
      if (pos != null) {
        sourceNodeMap.put(pos, gridNode);
        refreshNode(gridNode);
      }
    }
  }

  private SidedGlobalPos getSourcePosFromNode(IGridNode node) {
    Object owner = node.getOwner();

    if (!(owner instanceof PatternContainer)) return null;

    if (owner instanceof AEBasePart pt) {
      BlockEntity be = pt.getBlockEntity();
      if (be != null && be.getLevel() != null) {
        return SidedGlobalPos.of(
            GlobalPos.of(pt.getLevel().dimension(), be.getBlockPos()), pt.getSide());
      }
    } else if (owner instanceof BlockEntity be) {
      if (be.getLevel() != null) {
        return SidedGlobalPos.of(GlobalPos.of(be.getLevel().dimension(), be.getBlockPos()), null);
      }
    }
    return null;
  }

  @Override
  public void removeNode(IGridNode gridNode) {
    nodesToUpdate.remove(gridNode);

    SidedGlobalPos sPos = getSourcePosFromNode(gridNode);
    if (sPos != null) {
      if (sourceNodeMap.remove(sPos) != null) {
        refreshNode(gridNode);
      }
    }
    removeOldMirrorNode(gridNode);
  }

  private void removeOldMirrorNode(IGridNode gridNode) {
    SidedGlobalPos pos = mirrorReverseMap.remove(gridNode);
    if (pos == null) {
      return;
    }
    Set<IGridNode> mirrors = mirrorNodeMap.get(pos);
    if (mirrors != null) {
      mirrors.remove(gridNode);
      if (mirrors.isEmpty()) {
        mirrorNodeMap.remove(pos, mirrors);
      }
    }
  }
}

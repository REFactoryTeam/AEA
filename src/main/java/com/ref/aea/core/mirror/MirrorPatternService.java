package com.ref.aea.core.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.parts.AEBasePart;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.api.mirror.IMirrorPatternService;
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

  private final Map<IMirror.SourcePos, Set<IGridNode>> mirrorNodeMap = new ConcurrentHashMap<>();

  private final Map<IGridNode, IMirror.SourcePos> mirrorReverseMap = new ConcurrentHashMap<>();

  private final Map<IMirror.SourcePos, IGridNode> sourceNodeMap = new ConcurrentHashMap<>();

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
      if (mirror != null) {
        IMirror.SourcePos srcPos = mirror.getSourcePos();
        IGridNode sourceNode = null;

        if (srcPos != null) {
          sourceNode = sourceNodeMap.get(srcPos);

          if (sourceNode == null && srcPos.direction() != null) {
            sourceNode = sourceNodeMap.get(new IMirror.SourcePos(srcPos.globalPos(), null));
          }
        }
        mirror.updateSource(sourceNode);
      }
    }
  }

  @Override
  public void refreshNode(IGridNode node) {
    IMirror.SourcePos sourcePos = getSourcePosFromNode(node);
    if (sourcePos == null) return;
    if (sourcePos.direction() != null) {
      addMirrorsToUpdateQueue(sourcePos);
    } else {
      addMirrorsToUpdateQueue(sourcePos);
      for (Direction direction : Direction.values()) {
        addMirrorsToUpdateQueue(new IMirror.SourcePos(sourcePos.globalPos(), direction));
      }
    }
  }

  private void addMirrorsToUpdateQueue(IMirror.SourcePos pos) {
    Set<IGridNode> mirrors = mirrorNodeMap.get(pos);
    if (mirrors != null && !mirrors.isEmpty()) {
      nodesToUpdate.addAll(mirrors);
    }
  }

  public void updateMirrorPosition(IGridNode mirrorNode, IMirror.SourcePos newPos) {
    removeOldMirrorNode(mirrorNode);
    if (newPos != null) {
      mirrorReverseMap.put(mirrorNode, newPos);
      mirrorNodeMap.computeIfAbsent(newPos, k -> ConcurrentHashMap.newKeySet()).add(mirrorNode);
      nodesToUpdate.add(mirrorNode);
    }
  }

  @Override
  public void addNode(IGridNode gridNode, CompoundTag savedData) {
    var mirror = gridNode.getService(IMirror.class);

    if (mirror != null) {
      IMirror.SourcePos pos = mirror.getSourcePos();
      if (pos != null) {
        updateMirrorPosition(gridNode, pos);
        nodesToUpdate.add(gridNode);
      }
    } else {
      IMirror.SourcePos pos = getSourcePosFromNode(gridNode);
      if (pos != null) {
        sourceNodeMap.put(pos, gridNode);
        refreshNode(gridNode);
      }
    }
  }

  private IMirror.SourcePos getSourcePosFromNode(IGridNode node) {
    Object owner = node.getOwner();

    if (!(owner instanceof PatternContainer)) return null;

    if (owner.getClass().getName().endsWith(".TileAssemblerMatrixPattern")) return null;

    if (owner instanceof AEBasePart pt) {
      if (pt.getBlockEntity().getLevel() != null) {
        return new IMirror.SourcePos(
            GlobalPos.of(pt.getLevel().dimension(), pt.getBlockEntity().getBlockPos()),
            pt.getSide());
      }
    } else if (owner instanceof BlockEntity be) {
      if (be.getLevel() != null) {
        return new IMirror.SourcePos(
            GlobalPos.of(be.getLevel().dimension(), be.getBlockPos()), null);
      }
    }
    return null;
  }

  @Override
  public void removeNode(IGridNode gridNode) {
    nodesToUpdate.remove(gridNode);

    IMirror.SourcePos sPos = getSourcePosFromNode(gridNode);
    if (sPos != null) {
      sourceNodeMap.remove(sPos);
      refreshNode(gridNode);
    }

    removeOldMirrorNode(gridNode);
  }

  private void removeOldMirrorNode(IGridNode gridNode) {
    IMirror.SourcePos pos = mirrorReverseMap.remove(gridNode);
    if (pos == null) {
      return;
    }
    Set<IGridNode> mirrors = mirrorNodeMap.get(pos);
    if (mirrors == null) {
      return;
    }
    mirrors.remove(gridNode);
    if (mirrors.isEmpty()) {
      mirrorNodeMap.remove(pos);
    }
  }
}

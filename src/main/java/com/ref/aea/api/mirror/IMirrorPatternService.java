package com.ref.aea.api.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;

public interface IMirrorPatternService extends IGridService {
  void refreshNode(IGridNode node);

  void updateMirrorPosition(IGridNode node, IMirror.SourcePos newPos);
}

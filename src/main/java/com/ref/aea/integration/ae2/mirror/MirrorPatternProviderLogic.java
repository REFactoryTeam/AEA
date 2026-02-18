package com.ref.aea.integration.ae2.mirror;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.AEItemFilters;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.api.mixin.ae.crafting.mirror.IMixinPatternProviderLogic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MirrorPatternProviderLogic extends PatternProviderLogic
    implements IMirror<PatternProviderLogic> {

  private SourcePos sourcePos;

  private final Class<? extends PatternProviderLogicHost> targetPartClass;
  private final Class<? extends PatternProviderLogicHost> targetBeClass;

  public MirrorPatternProviderLogic(
      IManagedGridNode mainNode,
      PatternProviderLogicHost host,
      int patternInventorySize,
      Class<? extends PatternProviderLogicHost> targetPartClass,
      Class<? extends PatternProviderLogicHost> targetBeClass) {
    super(mainNode, host, patternInventorySize);
    this.targetPartClass = targetPartClass;
    this.targetBeClass = targetBeClass;
    ((AppEngInternalInventory) super.getPatternInv()).setFilter(AEItemFilters.EXTRACT_ONLY);
    mainNode.addService(IMirror.class, this);
  }

  @Override
  public void setSourcePos(@Nullable SourcePos sourcePos) {
    this.sourcePos = sourcePos;

    IMixinPatternProviderLogic thisMixin = (IMixinPatternProviderLogic) this;
    IGridNode node = thisMixin.AEA$getMainNode().getNode();
    if (node != null) {
      node.getGrid().getService(IMirrorPatternService.class).updateMirrorPosition(node, sourcePos);
    }
    this.updateSource(null);
  }

  @Override
  @Nullable
  public SourcePos getSourcePos() {
    return sourcePos;
  }

  @Override
  public Optional<PatternProviderLogic> getSource() {
    if (sourcePos == null) return Optional.empty();

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null || !server.isRunning()) return Optional.empty();

    ServerLevel level = server.getLevel(sourcePos.globalPos().dimension());
    if (level == null || !level.isLoaded(sourcePos.globalPos().pos())) return Optional.empty();

    BlockEntity be = level.getBlockEntity(sourcePos.globalPos().pos());

    PatternProviderLogic sourceLogic = null;

    if (be instanceof IPartHost iPartHost) {
      IPart part = iPartHost.getPart(sourcePos.direction());
      if (part != null && part.getClass() == targetPartClass) {
        sourceLogic = ((PatternProviderLogicHost) part).getLogic();
      }
    }

    if (be != null && be.getClass() == targetBeClass) {
      sourceLogic = ((PatternProviderLogicHost) be).getLogic();
    }

    if (sourceLogic == null || sourceLogic instanceof IMirror) {
      return Optional.empty();
    }

    return Optional.of(sourceLogic);
  }

  @Override
  public void updateSource(IGridNode node) {
    if (node != null) {
      Object owner = node.getOwner();
      if (owner.getClass() == targetPartClass || owner.getClass() == targetBeClass) {
        syncFromSource(((PatternProviderLogicHost) owner).getLogic());
        return;
      }
    }
    this.getSource().ifPresentOrElse(this::syncFromSource, this::clearLocalCache);
  }

  @Override
  public void writeToNBT(CompoundTag tag) {
    super.writeToNBT(tag);
    this.writeSourcePos(tag);
  }

  @Override
  public void readFromNBT(CompoundTag tag) {
    super.readFromNBT(tag);
    this.readSourcePos(tag);
  }

  @Override
  public void exportSettings(CompoundTag output) {
    this.writeSourcePos(output);
  }

  @Override
  public void importSettings(CompoundTag input, @Nullable Player player) {
    readSourcePos(input);
  }

  private void writeSourcePos(CompoundTag tag) {
    if (this.sourcePos != null) {
      IMirror.writeSourceToNBT(tag, this.sourcePos);
    }
  }

  private void readSourcePos(CompoundTag tag) {
    if (tag.contains(NBT_SOURCE_POS)) {
      IMirror.readSourceFromNBT(tag).ifPresent(pos -> this.sourcePos = pos);
    }
  }

  @Override
  public InternalInventory getPatternInv() {
    return this.getSource().map(PatternProviderLogic::getPatternInv).orElse(super.getPatternInv());
  }

  public void syncFromSource(PatternProviderLogic source) {
    IMixinPatternProviderLogic thisMixin = (IMixinPatternProviderLogic) this;
    IMixinPatternProviderLogic sourceMixin = (IMixinPatternProviderLogic) source;

    thisMixin.AEA$setPatterns(sourceMixin.AEA$getPatterns());
    thisMixin.AEA$setPatternInputs(sourceMixin.AEA$getPatternInputs());

    ICraftingProvider.requestUpdate(thisMixin.AEA$getMainNode());
  }

  private void clearLocalCache() {
    IMixinPatternProviderLogic thisMixin = (IMixinPatternProviderLogic) this;
    thisMixin.AEA$setPatterns(new ArrayList<>());
    thisMixin.AEA$setPatternInputs(new HashSet<>());
    ICraftingProvider.requestUpdate(thisMixin.AEA$getMainNode());
  }

  @Override
  public void onChangeInventory(InternalInventory inv, int slot) {}
}

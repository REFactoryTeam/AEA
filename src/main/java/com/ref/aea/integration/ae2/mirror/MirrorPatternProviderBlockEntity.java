package com.ref.aea.integration.ae2.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.parts.crafting.PatternProviderPart;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.integration.ae2.AE2Integration;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MirrorPatternProviderBlockEntity extends PatternProviderBlockEntity
    implements IMirror<PatternProviderLogic> {

  public MirrorPatternProviderBlockEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);
  }

  @Override
  protected PatternProviderLogic createLogic() {
    return new MirrorPatternProviderLogic(
        this.getMainNode(), this, 9, PatternProviderPart.class, PatternProviderBlockEntity.class);
  }

  @Override
  public MirrorPatternProviderLogic getLogic() {
    return (MirrorPatternProviderLogic) logic;
  }

  @Override
  public AEItemKey getTerminalIcon() {
    return AEItemKey.of(AE2Integration.MIRROR_PATTERN_PROVIDER_BLOCK.get());
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return AE2Integration.MIRROR_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
  }

  @Override
  public void addSidedGlobalPos(@NotNull SidedGlobalPos sourcePos) {
    this.getLogic().addSidedGlobalPos(sourcePos);
  }

  @Override
  public void clearSidedGlobalPos() {
    this.getLogic().clearSidedGlobalPos();
  }

  @Override
  public @NotNull Collection<SidedGlobalPos> getSidedGlobalPos() {
    return this.getLogic().getSidedGlobalPos();
  }

  @Override
  public Optional<PatternProviderLogic> getSource() {
    return this.getLogic().getSource();
  }

  @Override
  public void updateSource(IGridNode node) {
    this.getLogic().updateSource(node);
  }
}

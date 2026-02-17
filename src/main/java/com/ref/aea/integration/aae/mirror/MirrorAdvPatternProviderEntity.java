package com.ref.aea.integration.aae.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.integration.aae.AAEIntegration;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.definitions.AAEMenus;
import net.pedroksl.advanced_ae.common.entities.AdvPatternProviderEntity;
import net.pedroksl.advanced_ae.common.entities.SmallAdvPatternProviderEntity;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.parts.SmallAdvPatternProviderPart;
import org.jetbrains.annotations.Nullable;

public class MirrorAdvPatternProviderEntity extends AdvPatternProviderEntity
    implements IMirror<AdvPatternProviderLogic> {
  public MirrorAdvPatternProviderEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);
  }

  @Override
  protected MirrorAdvPatternProviderLogic createLogic(int slots) {
    return new MirrorAdvPatternProviderLogic(
        this.getMainNode(),
        this,
        9,
        SmallAdvPatternProviderPart.class,
        SmallAdvPatternProviderEntity.class);
  }

  @Override
  public MirrorAdvPatternProviderLogic getLogic() {
    return (MirrorAdvPatternProviderLogic) logic;
  }

  @Override
  public AEItemKey getTerminalIcon() {
    return AEItemKey.of(AAEIntegration.MIRROR_ADV_PATTERN_PROVIDER_BLOCK.get());
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return AAEIntegration.MIRROR_ADV_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
  }

  @Override
  public void openMenu(Player player, MenuLocator locator) {
    MenuOpener.open(AAEMenus.SMALL_ADV_PATTERN_PROVIDER.get(), player, locator);
  }

  @Override
  public void returnToMainMenu(Player player, ISubMenu subMenu) {
    MenuOpener.returnTo(AAEMenus.SMALL_ADV_PATTERN_PROVIDER.get(), player, subMenu.getLocator());
  }

  @Override
  public void setSourcePos(@Nullable IMirror.SourcePos sourcePos) {
    this.getLogic().setSourcePos(sourcePos);
  }

  @Override
  public @Nullable IMirror.SourcePos getSourcePos() {
    return this.getLogic().getSourcePos();
  }

  @Override
  public Optional<AdvPatternProviderLogic> getSource() {
    return this.getLogic().getSource();
  }

  @Override
  public void updateSource(IGridNode node) {
    this.getLogic().updateSource(node);
  }
}

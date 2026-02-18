package com.ref.aea.integration.eae.mirror;

import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.glodblock.github.extendedae.common.parts.PartExPatternProvider;
import com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider;
import com.glodblock.github.extendedae.container.ContainerExPatternProvider;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderBlockEntity;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderLogic;
import com.ref.aea.integration.eae.EAEIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MirrorEXPatternProviderBlockEntity extends MirrorPatternProviderBlockEntity {
  public MirrorEXPatternProviderBlockEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);
  }

  @Override
  protected PatternProviderLogic createLogic() {
    return new MirrorPatternProviderLogic(
        this.getMainNode(), this, 36, PartExPatternProvider.class, TileExPatternProvider.class);
  }

  @Override
  public AEItemKey getTerminalIcon() {
    return AEItemKey.of(EAEIntegration.MIRROR_EX_PATTERN_PROVIDER_BLOCK.get());
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return EAEIntegration.MIRROR_EX_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
  }

  @Override
  public void openMenu(Player player, MenuLocator locator) {
    MenuOpener.open(ContainerExPatternProvider.TYPE, player, locator);
  }

  @Override
  public void returnToMainMenu(Player player, ISubMenu subMenu) {
    MenuOpener.returnTo(ContainerExPatternProvider.TYPE, player, subMenu.getLocator());
  }
}

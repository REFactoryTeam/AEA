package com.ref.aea.integration.aae.mirror;

import appeng.api.stacks.AEItemKey;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.ref.aea.integration.aae.AAEIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.definitions.AAEMenus;
import net.pedroksl.advanced_ae.common.entities.AdvPatternProviderEntity;
import net.pedroksl.advanced_ae.common.parts.AdvPatternProviderPart;

public class MirrorEXAdvPatternProviderEntity extends MirrorAdvPatternProviderEntity {
  public MirrorEXAdvPatternProviderEntity(
      BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
    super(blockEntityType, pos, blockState);
  }

  @Override
  protected MirrorAdvPatternProviderLogic createLogic(int slots) {
    return new MirrorAdvPatternProviderLogic(
        this.getMainNode(), this, 36, AdvPatternProviderPart.class, AdvPatternProviderEntity.class);
  }

  @Override
  public AEItemKey getTerminalIcon() {
    return AEItemKey.of(AAEIntegration.MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK.get());
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return AAEIntegration.MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
  }

  @Override
  public void openMenu(Player player, MenuLocator locator) {
    MenuOpener.open(AAEMenus.ADV_PATTERN_PROVIDER.get(), player, locator);
  }

  @Override
  public void returnToMainMenu(Player player, ISubMenu subMenu) {
    MenuOpener.returnTo(AAEMenus.ADV_PATTERN_PROVIDER.get(), player, subMenu.getLocator());
  }
}

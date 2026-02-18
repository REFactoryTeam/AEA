package com.ref.aea.integration.eae.mirror;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.PartModel;
import com.glodblock.github.extendedae.common.parts.PartExPatternProvider;
import com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider;
import com.glodblock.github.extendedae.container.ContainerExPatternProvider;
import com.ref.aea.AEA;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderLogic;
import com.ref.aea.integration.ae2.mirror.MirrorPatternProviderPart;
import com.ref.aea.integration.eae.EAEIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MirrorEXPatternProviderPart extends MirrorPatternProviderPart {

  public static final ResourceLocation MODEL_BASE =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/mirror_ex_pattern_provider");

  @PartModels
  public static final PartModel MODELS_OFF =
      new PartModel(
          MODEL_BASE, ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "part/interface_off"));

  @PartModels
  public static final PartModel MODELS_ON =
      new PartModel(
          MODEL_BASE, ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "part/interface_on"));

  @PartModels
  public static final PartModel MODELS_HAS_CHANNEL =
      new PartModel(
          MODEL_BASE,
          ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "part/interface_has_channel"));

  public MirrorEXPatternProviderPart(IPartItem<?> partItem) {
    super(partItem);
  }

  @Override
  protected PatternProviderLogic createLogic() {
    return new MirrorPatternProviderLogic(
        this.getMainNode(), this, 36, PartExPatternProvider.class, TileExPatternProvider.class);
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return EAEIntegration.MIRROR_EX_PATTERN_PROVIDER_PART.get().getDefaultInstance();
  }

  @Override
  public IPartModel getStaticModels() {
    if (this.isActive() && this.isPowered()) {
      return MODELS_HAS_CHANNEL;
    } else {
      return this.isPowered() ? MODELS_ON : MODELS_OFF;
    }
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

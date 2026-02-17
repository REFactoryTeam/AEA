package com.ref.aea.integration.aae.mirror;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.PartModel;
import com.ref.aea.AEA;
import com.ref.aea.integration.aae.AAEIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.definitions.AAEMenus;
import net.pedroksl.advanced_ae.common.entities.AdvPatternProviderEntity;
import net.pedroksl.advanced_ae.common.parts.AdvPatternProviderPart;

public class MirrorEXAdvPatternProviderPart extends MirrorAdvPatternProviderPart {

  public static final ResourceLocation MODEL_BASE =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/mirror_ex_adv_pattern_provider");

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

  public MirrorEXAdvPatternProviderPart(IPartItem<?> partItem) {
    super(partItem, 36);
  }

  @Override
  protected MirrorAdvPatternProviderLogic createLogic(int slots) {
    return new MirrorAdvPatternProviderLogic(
        this.getMainNode(),
        this,
        slots,
        AdvPatternProviderPart.class,
        AdvPatternProviderEntity.class);
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return AAEIntegration.MIRROR_EX_ADV_PATTERN_PROVIDER_PART.get().getDefaultInstance();
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
    MenuOpener.open(AAEMenus.ADV_PATTERN_PROVIDER.get(), player, locator);
  }

  @Override
  public void returnToMainMenu(Player player, ISubMenu subMenu) {
    MenuOpener.returnTo(AAEMenus.ADV_PATTERN_PROVIDER.get(), player, subMenu.getLocator());
  }
}

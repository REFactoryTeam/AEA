package com.ref.aea.integration.aae.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import com.ref.aea.AEA;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.integration.aae.AAEIntegration;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.pedroksl.advanced_ae.common.definitions.AAEMenus;
import net.pedroksl.advanced_ae.common.entities.SmallAdvPatternProviderEntity;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.parts.AdvPatternProviderPart;
import net.pedroksl.advanced_ae.common.parts.SmallAdvPatternProviderPart;
import org.jetbrains.annotations.Nullable;

public class MirrorAdvPatternProviderPart extends AdvPatternProviderPart
    implements IMirror<AdvPatternProviderLogic> {

  public static final ResourceLocation MODEL_BASE =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/mirror_adv_pattern_provider");

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

  public MirrorAdvPatternProviderPart(IPartItem<?> partItem) {
    this(partItem, 9);
  }

  public MirrorAdvPatternProviderPart(IPartItem<?> partItem, int slots) {
    super(partItem, slots);
  }

  @Override
  protected MirrorAdvPatternProviderLogic createLogic(int slots) {
    return new MirrorAdvPatternProviderLogic(
        this.getMainNode(),
        this,
        slots,
        SmallAdvPatternProviderPart.class,
        SmallAdvPatternProviderEntity.class);
  }

  @Override
  public MirrorAdvPatternProviderLogic getLogic() {
    return (MirrorAdvPatternProviderLogic) logic;
  }

  @Override
  public ItemStack getMainMenuIcon() {
    return AAEIntegration.MIRROR_ADV_PATTERN_PROVIDER_PART.get().getDefaultInstance();
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
    MenuOpener.open(AAEMenus.SMALL_ADV_PATTERN_PROVIDER.get(), player, locator);
  }

  @Override
  public void returnToMainMenu(Player player, ISubMenu subMenu) {
    MenuOpener.returnTo(AAEMenus.SMALL_ADV_PATTERN_PROVIDER.get(), player, subMenu.getLocator());
  }

  @Override
  public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
    if (p.getCommandSenderWorld().isClientSide()) return true;
    ItemStack stack = p.getItemInHand(hand);
    if (stack.getTag() != null) {
      Optional<IMirror.SourcePos> sourcePos = IMirror.readSourceFromNBT(stack.getTag());
      if (sourcePos.isPresent()) {
        this.getLogic().setSourcePos(sourcePos.get());
        return true;
      }
    }
    openMenu(p, MenuLocators.forPart(this));
    return true;
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

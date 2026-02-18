package com.ref.aea.integration.ae2.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.AppEng;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import com.ref.aea.AEA;
import com.ref.aea.api.mirror.IMirror;
import com.ref.aea.integration.ae2.AE2Integration;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MirrorPatternProviderPart extends PatternProviderPart
    implements IMirror<PatternProviderLogic> {

  public static final ResourceLocation MODEL_BASE =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/mirror_pattern_provider");

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

  public MirrorPatternProviderPart(IPartItem<?> partItem) {
    super(partItem);
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
  public ItemStack getMainMenuIcon() {
    return AE2Integration.MIRROR_PATTERN_PROVIDER_PART.get().getDefaultInstance();
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
  public @Nullable SourcePos getSourcePos() {
    return this.getLogic().getSourcePos();
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

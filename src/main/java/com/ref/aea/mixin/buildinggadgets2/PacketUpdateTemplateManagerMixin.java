package com.ref.aea.mixin.buildinggadgets2;

import appeng.core.definitions.AEItems;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketUpdateTemplateManager;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.MiscHelpers;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.ref.aea.util.EncodedPatternUtil;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketUpdateTemplateManager.class, remap = false)
public abstract class PacketUpdateTemplateManagerMixin {

  @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
  private static void onHandle(
      PacketUpdateTemplateManager message,
      Supplier<NetworkEvent.Context> context,
      CallbackInfo ci) {
    NetworkEvent.Context ctx = context.get();
    ServerPlayer sender = ctx.getSender();
    if (sender == null) return;

    PacketUpdateTemplateManagerAccessor accessor = (PacketUpdateTemplateManagerAccessor) message;
    if (accessor.getMode() != 0) return;

    ci.cancel();

    ctx.enqueueWork(
        () -> {
          AbstractContainerMenu container = sender.containerMenu;
          if (!(container instanceof TemplateManagerContainer menu)) return;

          ItemStack templateStack = menu.getSlot(1).getItem();
          if (!templateStack.is(AEItems.BLANK_PATTERN.asItem())
              && !templateStack.is(AEItems.PROCESSING_PATTERN.asItem())) {
            return;
          }

          ItemStack gadgetStack = menu.getSlot(0).getItem();
          UUID sourceUUID = GadgetNBT.getUUID(gadgetStack);

          BG2Data bg2Data =
              BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
          ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);

          if (buildList == null || buildList.isEmpty()) {
            AEA$playFailSound(sender);
            return;
          }

          ItemStack encodedPattern =
              EncodedPatternUtil.fromBuildList(
                  sender.serverLevel(), sender, buildList, accessor.getTemplateName());

          if (!encodedPattern.isEmpty()) {
            menu.setItem(1, menu.getStateId(), encodedPattern);
            AEA$playSuccessSound(sender);
          }
        });
  }

  @Unique
  private static void AEA$playFailSound(ServerPlayer player) {
    MiscHelpers.playSound(
        player,
        Holder.direct(
            SoundEvent.createVariableRangeEvent(
                SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation())));
  }

  @Unique
  private static void AEA$playSuccessSound(ServerPlayer player) {
    MiscHelpers.playSound(
        player,
        Holder.direct(
            SoundEvent.createVariableRangeEvent(SoundEvents.ENCHANTMENT_TABLE_USE.getLocation())));
  }
}

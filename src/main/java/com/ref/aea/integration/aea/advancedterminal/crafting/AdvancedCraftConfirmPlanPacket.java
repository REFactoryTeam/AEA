package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.menu.me.crafting.CraftingPlanSummary;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class AdvancedCraftConfirmPlanPacket {

  private final CraftingPlanSummary plan;

  public AdvancedCraftConfirmPlanPacket(CraftingPlanSummary plan) {
    this.plan = plan;
  }

  public static void encode(AdvancedCraftConfirmPlanPacket msg, FriendlyByteBuf buffer) {
    msg.plan.write(buffer);
  }

  public static AdvancedCraftConfirmPlanPacket decode(FriendlyByteBuf buffer) {
    return new AdvancedCraftConfirmPlanPacket(CraftingPlanSummary.read(buffer));
  }

  public static void handle(
      AdvancedCraftConfirmPlanPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(() -> handleClient(msg));
    ctx.setPacketHandled(true);
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(AdvancedCraftConfirmPlanPacket msg) {
    Player player = Minecraft.getInstance().player;
    if (player != null && player.containerMenu instanceof AdvancedCraftConfirmMenu menu) {
      menu.setPlan(msg.plan);
    }
  }
}

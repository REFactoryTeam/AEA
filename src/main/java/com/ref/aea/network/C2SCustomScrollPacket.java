package com.ref.aea.network;

import com.ref.aea.api.common.ICustomScrollBehavior;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class C2SCustomScrollPacket {
  private final double delta;

  public C2SCustomScrollPacket(double delta) {
    this.delta = delta;
  }

  public static void encode(C2SCustomScrollPacket msg, FriendlyByteBuf buffer) {
    buffer.writeDouble(msg.delta);
  }

  public static C2SCustomScrollPacket decode(FriendlyByteBuf buffer) {
    return new C2SCustomScrollPacket(buffer.readDouble());
  }

  public static void handle(C2SCustomScrollPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(
        () -> {
          ServerPlayer player = ctx.getSender();
          if (player == null) return;
          ItemStack stack = player.getMainHandItem();
          if (stack.getItem() instanceof ICustomScrollBehavior scrollable) {
            scrollable.onScroll(stack, player, msg.delta);
          }
        });
    ctx.setPacketHandled(true);
  }
}

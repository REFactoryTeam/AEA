package com.ref.aea.integration.aea.advancedterminal.crafting;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AdvancedConfirmAutoCraftPacket {

  private final long amount;
  private final boolean craftMissingAmount;
  private final boolean autoStart;

  public AdvancedConfirmAutoCraftPacket(
      long amount, boolean craftMissingAmount, boolean autoStart) {
    this.amount = amount;
    this.craftMissingAmount = craftMissingAmount;
    this.autoStart = autoStart;
  }

  public static void encode(AdvancedConfirmAutoCraftPacket msg, FriendlyByteBuf buffer) {
    buffer.writeBoolean(msg.autoStart);
    buffer.writeBoolean(msg.craftMissingAmount);
    buffer.writeLong(msg.amount);
  }

  public static AdvancedConfirmAutoCraftPacket decode(FriendlyByteBuf buffer) {
    boolean autoStart = buffer.readBoolean();
    boolean craftMissingAmount = buffer.readBoolean();
    long amount = buffer.readLong();
    return new AdvancedConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart);
  }

  public static void handle(
      AdvancedConfirmAutoCraftPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(
        () -> {
          ServerPlayer player = ctx.getSender();
          if (player != null && player.containerMenu instanceof AdvancedCraftAmountMenu menu) {
            menu.confirm(msg.amount, msg.craftMissingAmount, msg.autoStart);
          }
        });
    ctx.setPacketHandled(true);
  }
}

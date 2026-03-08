package com.ref.aea.network;

import com.ref.aea.AEA;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AEANetwork {
  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel INSTANCE =
      NetworkRegistry.newSimpleChannel(
          ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "main_channel"),
          () -> PROTOCOL_VERSION,
          PROTOCOL_VERSION::equals,
          PROTOCOL_VERSION::equals);

  private static int packetId = 0;

  public static void register() {
    INSTANCE.registerMessage(
        packetId++,
        C2SCustomScrollPacket.class,
        C2SCustomScrollPacket::encode,
        C2SCustomScrollPacket::decode,
        C2SCustomScrollPacket::handle);
  }
}

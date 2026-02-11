package com.ref.aea.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KeyboardHandlerUtil {
  @OnlyIn(Dist.CLIENT)
  public static void setClipboard(Component localizedName) {
    Minecraft.getInstance().keyboardHandler.setClipboard(localizedName.getString());
  }
}

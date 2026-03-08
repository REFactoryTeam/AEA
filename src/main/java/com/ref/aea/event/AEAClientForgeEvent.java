package com.ref.aea.event;

import com.ref.aea.AEA;
import com.ref.aea.api.client.ILevelRenderItem;
import com.ref.aea.api.common.ICustomScrollBehavior;
import com.ref.aea.network.AEANetwork;
import com.ref.aea.network.C2SCustomScrollPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
    modid = AEA.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    value = Dist.CLIENT)
public class AEAClientForgeEvent {
  @SubscribeEvent
  public static void onRenderLevel(RenderLevelStageEvent event) {
    Minecraft mc = Minecraft.getInstance();
    Player player = mc.player;
    if (player == null) return;

    ItemStack mainHand = player.getMainHandItem();
    if (mainHand.getItem() instanceof ILevelRenderItem renderer) {
      renderer.renderLevelOverlay(event, mainHand);
    }

    ItemStack offHand = player.getOffhandItem();
    if (offHand.getItem() instanceof ILevelRenderItem renderer) {
      renderer.renderLevelOverlay(event, offHand);
    }

    mc.renderBuffers().bufferSource().endBatch();
  }

  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player == null) return;

    ItemStack stack = player.getMainHandItem();
    if (stack.getItem() instanceof ICustomScrollBehavior scrollable) {
      if (scrollable.shouldTrigger(player)) {
        double delta = event.getScrollDelta();
        AEANetwork.INSTANCE.sendToServer(new C2SCustomScrollPacket(delta));
        scrollable.onScroll(stack, player, delta);
        event.setCanceled(true);
      }
    }
  }
}

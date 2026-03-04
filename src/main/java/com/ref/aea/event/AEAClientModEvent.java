package com.ref.aea.event;

import appeng.core.definitions.AEItems;
import com.ref.aea.AEA;
import com.ref.aea.api.client.IRainbowRender;
import com.ref.aea.api.pos.SidedGlobalPos;
import com.ref.aea.core.definitions.AEAItems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
    modid = AEA.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public class AEAClientModEvent {

  public static final List<Supplier<? extends Block>> colorBlocks = new ArrayList<>();

  @SubscribeEvent
  public static void onAddPackFinders(AddPackFindersEvent event) {
    if (event.getPackType() == PackType.CLIENT_RESOURCES) {
      registerBuiltinPack(event, "dyeable_pattern", "Dyeable Pattern");
    }
  }

  private static void registerBuiltinPack(
      AddPackFindersEvent event, String folderName, String title) {
    String packId = AEA.MOD_ID + ":" + folderName;
    ModList.get()
        .getModContainerById(AEA.MOD_ID)
        .ifPresent(
            container -> {
              Path resourcePath =
                  container
                      .getModInfo()
                      .getOwningFile()
                      .getFile()
                      .findResource("resourcepacks/" + folderName);

              if (Files.exists(resourcePath)) {
                Pack pack =
                    Pack.readMetaAndCreate(
                        packId,
                        Component.literal(title),
                        false,
                        (id) -> new PathPackResources(id, resourcePath, false),
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN);

                if (pack != null) {
                  event.addRepositorySource(consumer -> consumer.accept(pack));
                }
              }
            });
  }

  @SubscribeEvent
  public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    event.register(
        AEAClientModEvent::getColorForDyeableItem,
        AEItems.CRAFTING_PATTERN,
        AEItems.PROCESSING_PATTERN,
        AEItems.SMITHING_TABLE_PATTERN,
        AEItems.STONECUTTING_PATTERN);
    event.register(AEAClientModEvent::getColorForTime, AEAItems.MIRROR_CONNECTION_TOOL.get());
  }

  @SubscribeEvent
  public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
    colorBlocks.forEach(
        block -> event.register(AEAClientModEvent::getColorForBlockPos, block.get()));
  }

  public static int getColorForDyeableItem(ItemStack stack, int tintIndex) {
    if (tintIndex == 1 && stack.getItem() instanceof DyeableLeatherItem dyeableEncodedPattern) {
      return dyeableEncodedPattern.getColor(stack);
    } else {
      return -1;
    }
  }

  public static int getColorForTime(ItemStack stack, int tintIndex) {
    if (SidedGlobalPos.fromNbt(stack.getTag()).isPresent()) {
      return IRainbowRender.INSTANCE.getRainbowColor(System.currentTimeMillis(), 0.0f);
    }
    return -1;
  }

  public static int getColorForBlockPos(
      BlockState pState,
      @Nullable BlockAndTintGetter pLevel,
      @Nullable BlockPos pPos,
      int pTintIndex) {
    if (pTintIndex == 1) {
      if (pPos == null) {
        return -1;
      }
      float scale = 20.0f;
      float hue = (pPos.getX() + pPos.getY() + pPos.getZ()) / scale;
      hue = hue % 1.0f;
      if (hue < 0) hue += 1.0f;
      return java.awt.Color.HSBtoRGB(hue, 0.7f, 0.9f);
    }
    return -1;
  }
}

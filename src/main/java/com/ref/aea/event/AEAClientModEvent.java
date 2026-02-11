package com.ref.aea.event;

import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import com.ref.aea.AEA;
import com.ref.aea.core.modifier.PatternEncodingModifierService;
import com.ref.aea.integration.create.MechanicalCraftingRecipePatternEncodingModifier;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(
    modid = AEA.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public class AEAClientModEvent {

  @SubscribeEvent
  public static void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(
        () -> {
          PatternEncodingModifierService.register(
              MechanicalCraftingRecipePatternEncodingModifier.INSTANCE);
        });
  }

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
        AEAClientModEvent::getTintColor,
        AEItems.CRAFTING_PATTERN,
        AEItems.PROCESSING_PATTERN,
        AEItems.SMITHING_TABLE_PATTERN,
        AEItems.STONECUTTING_PATTERN);
  }

  public static int getTintColor(ItemStack stack, int tintIndex) {
    if (tintIndex == 1
        && stack.getItem() instanceof EncodedPatternItem encodedPattern
        && encodedPattern instanceof DyeableLeatherItem dyeableEncodedPattern) {
      return dyeableEncodedPattern.getColor(stack);
    } else {
      return 0xFFFFFF;
    }
  }
}

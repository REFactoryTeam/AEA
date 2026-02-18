package com.ref.aea.data;

import com.ref.aea.AEA;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AEA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AEADataGenerators {
  @SubscribeEvent
  public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    DataGenerator.PackGenerator pack = generator.getVanillaPack(true);

    generator.addProvider(event.includeClient(), new AEALangProvider(packOutput));
    var blockTagsProvider =
        pack.addProvider(c -> new AEABlockTagsProvider(c, lookupProvider, existingFileHelper));
    pack.addProvider(
        c ->
            new AEAItemTagsProvider(
                c, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    pack.addProvider(c -> new AEAFluidTagsProvider(c, lookupProvider, existingFileHelper));
  }
}

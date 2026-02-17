package com.ref.aea;

import appeng.api.networking.GridServices;
import com.mojang.logging.LogUtils;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.config.AEAClientConfig;
import com.ref.aea.config.AEAServerConfig;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.core.mirror.MirrorPatternService;
import com.ref.aea.integration.ae2.AE2Integration;
import com.ref.aea.integration.create.CreateIntegration;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AEA.MOD_ID)
public class AEA {
  public static final String MOD_ID = "aea";

  public static final Logger LOGGER = LogUtils.getLogger();

  public static boolean AE2 = ModList.get().isLoaded("ae2");
  public static boolean CREATE = ModList.get().isLoaded("create");

  public AEA(FMLJavaModLoadingContext context) {
    if (AE2) {
      AE2Integration.init();
    }
    if (CREATE) {
      CreateIntegration.init();
    }

    AEAItems.DR.register(context.getModEventBus());
    AEABlocks.DR.register(context.getModEventBus());
    AEABlockEntityType.DR.register(context.getModEventBus());

    context.registerConfig(ModConfig.Type.CLIENT, AEAClientConfig.SPEC);
    context.registerConfig(ModConfig.Type.SERVER, AEAServerConfig.SPEC);

    GridServices.register(IMirrorPatternService.class, MirrorPatternService.class);
  }
}

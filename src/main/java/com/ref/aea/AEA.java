package com.ref.aea;

import com.mojang.logging.LogUtils;
import com.ref.aea.config.AEAClientConfig;
import com.ref.aea.config.AEACommonConfig;
import com.ref.aea.config.AEAServerConfig;
import com.ref.aea.core.definitions.AEABlockEntityType;
import com.ref.aea.core.definitions.AEABlocks;
import com.ref.aea.core.definitions.AEAItems;
import com.ref.aea.core.definitions.AEATab;
import com.ref.aea.event.common.AEAInitMenuTypes;
import com.ref.aea.integration.aae.AAEIntegration;
import com.ref.aea.integration.ae2.AE2Integration;
import com.ref.aea.integration.aea.AEAIntegration;
import com.ref.aea.integration.create.CreateIntegration;
import com.ref.aea.integration.eae.EAEIntegration;
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
  public static boolean EAE = ModList.get().isLoaded("expatternprovider");
  public static boolean AAE = ModList.get().isLoaded("advanced_ae");
  public static boolean CREATE = ModList.get().isLoaded("create");

  public AEA(FMLJavaModLoadingContext context) {
    AEAIntegration.init();
    if (AE2) {
      AE2Integration.init();
    }
    if (EAE) {
      EAEIntegration.init();
    }
    if (AAE) {
      AAEIntegration.init();
    }
    if (CREATE) {
      CreateIntegration.init();
    }

    AEAItems.DR.register(context.getModEventBus());
    AEABlocks.DR.register(context.getModEventBus());
    AEABlockEntityType.DR.register(context.getModEventBus());
    AEATab.DR.register(context.getModEventBus());

    AEAInitMenuTypes.init();

    context.registerConfig(ModConfig.Type.CLIENT, AEAClientConfig.SPEC);
    context.registerConfig(ModConfig.Type.SERVER, AEAServerConfig.SPEC);
    context.registerConfig(ModConfig.Type.COMMON, AEACommonConfig.SPEC);
  }
}

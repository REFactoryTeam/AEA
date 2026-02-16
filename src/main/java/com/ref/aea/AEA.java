package com.ref.aea;

import com.mojang.logging.LogUtils;
import com.ref.aea.config.AEAClientConfig;
import com.ref.aea.config.AEAServerConfig;
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

  public static boolean CREATE = ModList.get().isLoaded("create");

  public AEA(FMLJavaModLoadingContext context) {
    if (CREATE) {
      CreateIntegration.init();
    }
    context.registerConfig(ModConfig.Type.CLIENT, AEAClientConfig.SPEC);
    context.registerConfig(ModConfig.Type.SERVER, AEAServerConfig.SPEC);
  }
}

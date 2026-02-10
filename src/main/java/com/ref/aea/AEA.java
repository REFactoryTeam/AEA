package com.ref.aea;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AEA.MOD_ID)
public class AEA {
  public static final String MOD_ID = "aea";

  public static final Logger LOGGER = LogUtils.getLogger();

  public AEA(FMLJavaModLoadingContext context) {}
}

package com.ref.aea.integration.create;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CreateIntegration {
  public static void init() {
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CreateIntegrationClient::init);
  }
}

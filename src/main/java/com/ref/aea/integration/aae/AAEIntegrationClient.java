package com.ref.aea.integration.aae;

import com.ref.aea.event.AEAClientModEvent;

public class AAEIntegrationClient {
  public static void init() {
    AEAClientModEvent.colorBlocks.add(AAEIntegration.MIRROR_ADV_PATTERN_PROVIDER_BLOCK);
    AEAClientModEvent.colorBlocks.add(AAEIntegration.MIRROR_EX_ADV_PATTERN_PROVIDER_BLOCK);
  }
}

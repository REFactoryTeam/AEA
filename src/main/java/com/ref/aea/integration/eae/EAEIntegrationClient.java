package com.ref.aea.integration.eae;

import com.ref.aea.event.AEAClientModEvent;

public class EAEIntegrationClient {
  public static void init() {
    AEAClientModEvent.colorBlocks.add(EAEIntegration.MIRROR_EX_PATTERN_PROVIDER_BLOCK);
  }
}

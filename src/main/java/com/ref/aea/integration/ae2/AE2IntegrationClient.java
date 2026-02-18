package com.ref.aea.integration.ae2;

import com.ref.aea.event.AEAClientModEvent;

public class AE2IntegrationClient {
  public static void init() {
    AEAClientModEvent.colorBlocks.add(AE2Integration.MIRROR_PATTERN_PROVIDER_BLOCK);
  }
}

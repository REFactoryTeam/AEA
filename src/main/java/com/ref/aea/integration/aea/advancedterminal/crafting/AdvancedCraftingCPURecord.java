package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.api.networking.crafting.ICraftingCPU;
import net.minecraft.network.chat.Component;

public class AdvancedCraftingCPURecord implements Comparable<AdvancedCraftingCPURecord> {
  private final ICraftingCPU cpu;
  private final long size;
  private final int processors;
  private Component name;

  public AdvancedCraftingCPURecord(long size, int coProcessors, ICraftingCPU server) {
    this.size = size;
    this.processors = coProcessors;
    this.cpu = server;
    this.name = server.getName();
  }

  @Override
  public int compareTo(AdvancedCraftingCPURecord o) {
    final int a = Long.compare(o.getProcessors(), this.getProcessors());
    if (a != 0) {
      return a;
    }
    return Long.compare(o.getSize(), this.getSize());
  }

  ICraftingCPU getCpu() {
    return this.cpu;
  }

  int getProcessors() {
    return this.processors;
  }

  long getSize() {
    return this.size;
  }

  public Component getName() {
    return name;
  }

  public void setName(Component name) {
    this.name = name;
  }
}

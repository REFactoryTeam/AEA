package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingService;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;

/** Utility class for dialogs that can cycle through crafting CPUs */
class AdvancedCraftingCPUCycler {

  @FunctionalInterface
  public interface ChangeListener {
    void onChange(AdvancedCraftingCPURecord selectedCpu, boolean cpusAvailable);
  }

  private final Predicate<ICraftingCPU> cpuFilter;
  private final ChangeListener changeListener;
  private final List<AdvancedCraftingCPURecord> cpus = new ArrayList<>();
  private int selectedCpu = -1;
  private boolean initialDataSent = false;
  private boolean allowNoSelection;

  public AdvancedCraftingCPUCycler(
      Predicate<ICraftingCPU> cpuFilter, ChangeListener changeListener) {
    this.cpuFilter = cpuFilter;
    this.changeListener = changeListener;
  }

  public void detectAndSendChanges(IGrid network) {
    final ICraftingService cc = network.getCraftingService();
    final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

    int matches = 0;
    boolean changed = !initialDataSent;
    initialDataSent = true;
    for (ICraftingCPU c : cpuSet) {
      boolean found = false;
      for (AdvancedCraftingCPURecord ccr : this.cpus) {
        if (ccr.getCpu() == c) {
          found = true;
          break;
        }
      }

      final boolean matched = this.cpuFilter.test(c);

      if (matched) {
        matches++;
      }

      if (found != matched) {
        changed = true;
      }
    }

    if (changed || this.cpus.size() != matches) {
      this.cpus.clear();
      for (ICraftingCPU c : cpuSet) {
        if (this.cpuFilter.test(c)) {
          this.cpus.add(
              new AdvancedCraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
        }
      }

      // Sort and assign numeric IDs in case they have no names
      Collections.sort(this.cpus);
      for (int i = 0; i < this.cpus.size(); i++) {
        AdvancedCraftingCPURecord cpu = cpus.get(i);
        if (cpu.getName() == null) {
          cpu.setName(Component.literal("#" + (i + 1)));
        }
      }

      this.notifyListener();
    }
  }

  public void cycleCpu(boolean next) {
    if (next) {
      this.selectedCpu++;
    } else {
      this.selectedCpu--;
    }

    // If "no CPU" is a valid selection, then -1 is the first potential item
    int lowerLimit = this.allowNoSelection ? -1 : 0;

    if (this.selectedCpu < lowerLimit) {
      this.selectedCpu = this.cpus.size() - 1;
    } else if (this.selectedCpu >= this.cpus.size()) {
      this.selectedCpu = lowerLimit;
    }

    this.notifyListener();
  }

  public boolean isAllowNoSelection() {
    return allowNoSelection;
  }

  public void setAllowNoSelection(boolean allowNoSelection) {
    this.allowNoSelection = allowNoSelection;
  }

  private void notifyListener() {
    if (this.selectedCpu >= this.cpus.size()) {
      this.selectedCpu = -1;
    }

    // Force the selected CPU to the first available CPU unless no-selection is
    // explicitly allowed
    if (!this.allowNoSelection && this.selectedCpu == -1 && !this.cpus.isEmpty()) {
      this.selectedCpu = 0;
    }

    if (this.selectedCpu != -1) {
      this.changeListener.onChange(this.cpus.get(this.selectedCpu), true);
    } else {
      this.changeListener.onChange(null, !this.cpus.isEmpty());
    }
  }
}

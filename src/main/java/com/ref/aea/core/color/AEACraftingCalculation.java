package com.ref.aea.core.color;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.hooks.ticking.TickHandler;
import com.google.common.base.Stopwatch;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEACraftingCalculation extends CraftingCalculation {
  public final NetworkCraftingSimulationState networkInv;
  private final Level level;
  private final KeyCounter missing = new KeyCounter();
  private final Object monitor = new Object();
  private final Stopwatch watch = Stopwatch.createUnstarted();
  private final AEACraftingTreeNode tree;
  private final AEKey output;
  private final long requestedAmount;
  private final CalculationStrategy strategy;
  private boolean simulate = false;
  final ICraftingSimulationRequester simRequester;
  private boolean running = false;
  private boolean done = false;
  private int time = 5;
  private int incTime = Integer.MAX_VALUE;
  private final Map<Integer, Set<AEKey>> failedRingReplacements = new HashMap<>();
  private final Set<Integer> ringsBeingReplaced = new HashSet<>();
  public final KeyCounter ringExtractions = new KeyCounter();

  public AEACraftingCalculation(
      Level level,
      IGrid grid,
      ICraftingSimulationRequester simRequester,
      GenericStack output,
      CalculationStrategy strategy) {
    super(level, grid, simRequester, output, strategy);
    this.level = level;
    this.output = output.what();
    this.requestedAmount = output.amount();
    this.strategy = strategy;
    this.simRequester = simRequester;

    var storage = grid.getStorageService();
    var craftingService = grid.getCraftingService();
    this.networkInv = new NetworkCraftingSimulationState(storage, simRequester.getActionSource());

    this.tree = new AEACraftingTreeNode(craftingService, this, this.output, 1, null, -1);
  }

  void addMissing(AEKey what, long amount) {
    missing.add(what, amount);
  }

  @Override
  public ICraftingPlan run() {
    try {
      TickHandler.instance().registerCraftingSimulation(this.level, this);
      this.handlePausing();
      return computePlan();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      this.finish();
    }
  }

  private ICraftingPlan computePlan() throws InterruptedException {
    var fullAmountPlan = runCraftAttempt(false, requestedAmount);
    if (fullAmountPlan != null) {
      return fullAmountPlan;
    }

    if (strategy == CalculationStrategy.CRAFT_LESS) {
      long successfulAmount = 0;
      ICraftingPlan successfulPlan = null;
      for (long increment = Long.highestOneBit(requestedAmount); increment > 0; increment /= 2) {
        long testAmount = successfulAmount + increment;
        if (testAmount < requestedAmount) {
          var plan = runCraftAttempt(false, testAmount);
          if (plan != null) {
            successfulAmount = testAmount;
            successfulPlan = plan;
          }
        }
      }
      if (successfulPlan != null) {
        return successfulPlan;
      }
    }
    return runCraftAttempt(true, requestedAmount);
  }

  @Nullable
  @Contract("true, _ -> !null")
  private CraftingPlan runCraftAttempt(boolean simulate, long amount) throws InterruptedException {
    this.simulate = simulate;
    this.ringExtractions.reset();
    ChildCraftingSimulationState craftingInventory = new ChildCraftingSimulationState(networkInv);
    craftingInventory.ignore(this.output);

    try {
      this.tree.request(craftingInventory, amount, null, false);
    } catch (CraftBranchFailure failure) {
      return null;
    }
    craftingInventory.addBytes(this.tree.getNodeCount() * 8);
    CraftingPlan basePlan =
        CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);

    if (!this.ringExtractions.isEmpty()) {
      return mergeRingExtractions(basePlan);
    }

    return basePlan;
  }

  private CraftingPlan mergeRingExtractions(CraftingPlan basePlan) {
    KeyCounter combinedUsedItems = new KeyCounter();
    combinedUsedItems.addAll(basePlan.usedItems());
    combinedUsedItems.addAll(this.ringExtractions);

    GenericStack finalOutput = getOutput(basePlan);

    return new CraftingPlan(
        finalOutput,
        basePlan.bytes(),
        basePlan.simulation(),
        basePlan.multiplePaths(),
        combinedUsedItems,
        basePlan.emittedItems(),
        basePlan.missingItems(),
        basePlan.patternTimes());
  }

  private @NotNull GenericStack getOutput(CraftingPlan basePlan) {
    AEKey targetKey = basePlan.finalOutput().what();
    long totalOutput = 0;
    for (var entry : basePlan.patternTimes().entrySet()) {
      IPatternDetails pattern = entry.getKey();
      long times = entry.getValue();

      for (GenericStack out : pattern.getOutputs()) {
        if (out.what().equals(targetKey)) {
          totalOutput += out.amount() * times;
        }
      }
    }
    return totalOutput > basePlan.finalOutput().amount()
        ? new GenericStack(targetKey, totalOutput)
        : basePlan.finalOutput();
  }

  void handlePausing() throws InterruptedException {
    if (this.incTime > 100) {
      this.incTime = 0;
      synchronized (this.monitor) {
        if (this.watch.elapsed(TimeUnit.MICROSECONDS) > this.time) {
          this.running = false;
          this.watch.stop();
          this.monitor.notify();
        }
        if (!this.running) {
          while (!this.running) {
            this.monitor.wait();
          }
        }
      }
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    }
    this.incTime++;
  }

  private void finish() {
    synchronized (this.monitor) {
      this.running = false;
      this.done = true;
      this.monitor.notify();
    }
  }

  @Override
  public boolean isSimulation() {
    return this.simulate;
  }

  @Override
  public AEKey getOutput() {
    return output;
  }

  @Override
  public KeyCounter getMissingItems() {
    return missing;
  }

  Level getLevel() {
    return this.level;
  }

  @Override
  public boolean simulateFor(int micros) {
    this.time = micros;
    synchronized (this.monitor) {
      if (this.done) {
        return false;
      }
      this.watch.reset();
      this.watch.start();
      this.running = true;
      this.monitor.notify();
      while (this.running) {
        try {
          this.monitor.wait();
        } catch (InterruptedException ignored) {
        }
      }
    }
    return true;
  }

  @Override
  public boolean hasMultiplePaths() {
    return this.tree.hasMultiplePaths();
  }

  public boolean hasRingReplacementFailed(int color, AEKey entryPoint) {
    Set<AEKey> failures = failedRingReplacements.get(color);
    return failures != null && failures.contains(entryPoint);
  }

  public void markRingReplacementAsFailed(int color, AEKey entryPoint) {
    failedRingReplacements.computeIfAbsent(color, k -> new HashSet<>()).add(entryPoint);
  }

  public Set<Integer> getRingsBeingReplaced() {
    return this.ringsBeingReplaced;
  }

  public long trackRingUsage(AEKey key, long amount) {
    long extracted = this.networkInv.extract(key, amount, Actionable.MODULATE);
    if (extracted > 0) {
      this.ringExtractions.add(key, extracted);
    }
    return extracted;
  }
}

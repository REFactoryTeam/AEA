package com.ref.aea.core.color;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;
import com.ref.aea.api.mixin.ae.crafting.color.CraftingPlanCompressedRing;
import com.ref.aea.api.mixin.ae.crafting.color.IMixinCraftingService;
import com.ref.aea.api.mixin.ae.crafting.color.IMixinPatternDetails;
import com.ref.aea.api.mixin.ae.crafting.color.RingReplacementTriggeredException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AEACraftingTreeNode {
  @Nullable final IPatternDetails.IInput parentInput;
  private final AEACraftingCalculation job;
  private final AEACraftingTreeProcess parent;
  private final Level level;
  private final AEKey what;
  private final long amount;
  private ArrayList<AEACraftingTreeProcess> nodes = null;
  private final boolean canEmit;
  private final int color;

  public AEACraftingTreeNode(
      ICraftingService cc,
      AEACraftingCalculation job,
      AEKey what,
      long amount,
      AEACraftingTreeProcess par,
      int slot) {
    this.parent = par;
    this.parentInput = slot == -1 ? null : par.details.getInputs()[slot];
    this.level = job.getLevel();
    this.job = job;
    this.what = findCraftedStack(cc, what);
    this.amount = amount;
    this.canEmit = cc.canEmitFor(what);
    this.color = par == null ? -1 : ((IMixinPatternDetails) par.details).AEA$getColor();
  }

  private AEKey findCraftedStack(ICraftingService cc, AEKey wat) {
    if (cc.canEmitFor(wat)) {
      return wat;
    }
    var patterns = cc.getCraftingFor(wat);
    if (patterns.isEmpty() && parentInput != null) {
      long acceptableAmount = parentInput.getPossibleInputs()[0].amount();
      for (var possibleInput : parentInput.getPossibleInputs()) {
        if (possibleInput.amount() != acceptableAmount) {
          continue;
        }
        var fuzzy =
            cc.getFuzzyCraftable(
                possibleInput.what(),
                fuzzyCandidate -> this.parentInput.isValid(fuzzyCandidate, level));
        if (fuzzy != null) {
          return fuzzy;
        }
      }
    }
    return wat;
  }

  private void buildChildPatterns() {
    if (this.canEmit) {
      throw new IllegalStateException("Internal AE2 error: emitable node shouldn't use patterns!");
    }
    if (this.nodes != null) {
      return;
    }
    this.nodes = new ArrayList<>();
    var gridNode = this.job.simRequester.getGridNode();

    if (gridNode == null) {
      return;
    }
    var craftingService = gridNode.getGrid().getCraftingService();
    var allPatterns = craftingService.getCraftingFor(this.what);

    var validPatterns =
        allPatterns.stream()
            .filter(details -> this.parent == null || this.parent.notRecursive(details));

    if (this.color != -1) {
      validPatterns =
          validPatterns.sorted(
              Comparator.comparingInt(
                  details ->
                      ((IMixinPatternDetails) details).AEA$getColor() == this.color ? 0 : 1));
    }

    validPatterns.forEach(
        details -> this.nodes.add(new AEACraftingTreeProcess(craftingService, job, details, this)));
  }

  boolean notRecursive(IPatternDetails details) {
    for (var output : details.getOutputs()) {
      if (this.what.matches(output)) {
        return false;
      }
    }
    for (var input : details.getInputs()) {
      if (this.what.matches(input.getPossibleInputs()[0])) {
        return false;
      }
    }
    return this.parent == null || this.parent.notRecursive(details);
  }

  private void addContainerItems(AEKey template, long multiplier, @Nullable KeyCounter outputList) {
    if (outputList != null) {
      AEKey containerItem = null;
      if (parentInput != null) {
        containerItem = parentInput.getRemainingKey(template);
      }
      if (containerItem != null) {
        outputList.add(containerItem, multiplier);
      }
    }
  }

  void request(
      CraftingSimulationState inv,
      long requestedAmount,
      @Nullable KeyCounter containerItems,
      boolean isCaptureSet)
      throws CraftBranchFailure, InterruptedException {

    this.job.handlePausing();
    inv.addStackBytes(what, amount, requestedAmount);

    for (var template : getValidItemTemplates(inv)) {
      long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);
      if (extracted > 0) {
        requestedAmount -= extracted;
        addContainerItems(template.key(), extracted, containerItems);
        if (requestedAmount == 0) {
          return;
        }
      }
    }

    addContainerItems(what, requestedAmount, containerItems);

    if (this.canEmit) {
      inv.emitItems(this.what, this.amount * requestedAmount);
      return;
    }

    buildChildPatterns();
    long totalRequestedItems = requestedAmount * this.amount;

    for (AEACraftingTreeProcess pro : this.nodes) {
      boolean shouldSetUpCapture = false;
      final int processColor = ((IMixinPatternDetails) pro.details).AEA$getColor();

      if (processColor != -1) {
        var craftingService =
            Objects.requireNonNull(this.job.simRequester.getGridNode())
                .getGrid()
                .getCraftingService();
        var ring = ((IMixinCraftingService) craftingService).AEA$getCompressedRing(processColor);
        if (ring != null && ring.entryPoints().contains(this.what)) {
          if (!isCaptureSet && !this.job.hasRingReplacementFailed(processColor, this.what)) {
            shouldSetUpCapture = true;
          }
        }
      }

      if (shouldSetUpCapture) {
        try {
          totalRequestedItems -= executeProcess(inv, pro, totalRequestedItems, true);
        } catch (RingReplacementTriggeredException e) {
          long fulfilledByRing = tryRingReplacement(inv, totalRequestedItems, processColor);
          if (fulfilledByRing > 0) {
            totalRequestedItems -= fulfilledByRing;
          } else {
            this.job.markRingReplacementAsFailed(processColor, this.what);
            totalRequestedItems -= executeProcess(inv, pro, totalRequestedItems, false);
          }
        }
      } else {
        totalRequestedItems -= executeProcess(inv, pro, totalRequestedItems, isCaptureSet);
      }
    }

    if (totalRequestedItems > 0) {
      if (this.job.isSimulation()) {
        job.addMissing(this.what, totalRequestedItems);
      } else {
        throw new CraftBranchFailure(this.what, totalRequestedItems);
      }
    }
  }

  private Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv) {
    if (this.parentInput == null) return List.of(new InputTemplate(what, 1));
    return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, level);
  }

  long getNodeCount() {
    long tot = 1;
    if (this.nodes != null) {
      for (AEACraftingTreeProcess pro : this.nodes) {
        tot += pro.getNodeCount();
      }
    }
    return tot;
  }

  boolean hasMultiplePaths() {
    if (this.nodes == null) return false;
    if (this.nodes.size() > 1) return true;
    for (var pro : this.nodes) {
      if (pro.hasMultiplePaths()) return true;
    }
    return false;
  }

  private long executeProcess(
      CraftingSimulationState inv,
      AEACraftingTreeProcess pro,
      long neededAmount,
      boolean isCaptureSet)
      throws InterruptedException {
    if (isCaptureSet
        && this.color != -1
        && ((IMixinPatternDetails) pro.details).AEA$getColor() != this.color) {
      throw new RingReplacementTriggeredException();
    }

    long totalProduced = 0;
    while (neededAmount > 0) {
      long producedInThisAttempt = 0;
      try {
        final ChildCraftingSimulationState attemptInv = new ChildCraftingSimulationState(inv);
        var craftedPerPattern = pro.getOutputCount(this.what);
        if (craftedPerPattern <= 0) break;

        long timesToCraft =
            pro.limitQty ? 1 : (neededAmount + craftedPerPattern - 1) / craftedPerPattern;
        pro.request(attemptInv, timesToCraft, isCaptureSet);

        producedInThisAttempt = attemptInv.extract(this.what, neededAmount, Actionable.MODULATE);
        if (producedInThisAttempt > 0) {
          attemptInv.applyDiff(inv);
          neededAmount -= producedInThisAttempt;
          totalProduced += producedInThisAttempt;
        }
      } catch (CraftBranchFailure ignored) {
        if (isCaptureSet) {
          throw new RingReplacementTriggeredException();
        }
      }
      if (producedInThisAttempt == 0) {
        break;
      }
    }
    return totalProduced;
  }

  private long tryRingReplacement(CraftingSimulationState inv, long requestedAmount, int ringColor)
      throws InterruptedException {
    IGrid grid = Objects.requireNonNull(job.simRequester.getGridNode()).getGrid();
    var craftingService = grid.getCraftingService();
    var ring = ((IMixinCraftingService) craftingService).AEA$getCompressedRing(ringColor);

    if (ring == null || !ring.isCalculable()) {
      this.job.markRingReplacementAsFailed(ringColor, this.what);
      return 0;
    }

    var ringsBeingReplaced = this.job.getRingsBeingReplaced();
    if (ringsBeingReplaced.contains(ringColor)) {
      return 0;
    }
    ringsBeingReplaced.add(ringColor);

    AEACraftingTreeNode entryNode = null;
    AEACraftingTreeNode cursor = this;
    while (cursor != null) {
      if (ring.entryPoints().contains(cursor.what)) {
        entryNode = cursor;
      }
      cursor = cursor.parent != null ? cursor.parent.parent : null;
    }

    if (entryNode == null) {
      ringsBeingReplaced.remove(ringColor);
      return 0;
    }

    final ChildCraftingSimulationState sandbox = new ChildCraftingSimulationState(inv);

    try {
      long ringNetOutputAmount = ring.netOutputs().get(entryNode.what);
      if (ringNetOutputAmount <= 0) {
        this.job.markRingReplacementAsFailed(ringColor, this.what);
        ringsBeingReplaced.remove(ringColor);
        return 0;
      }
      double scale = (double) requestedAmount / ringNetOutputAmount;
      requestRingDependencies(sandbox, craftingService, ring, scale);
      unpackRingOperations(sandbox, ring, scale);

      long fulfilled = sandbox.extract(this.what, requestedAmount, Actionable.MODULATE);
      if (fulfilled > 0) {
        sandbox.applyDiff(inv);
        ringsBeingReplaced.remove(ringColor);
        return fulfilled;
      }
    } catch (CraftBranchFailure ignored) {

    }

    this.job.markRingReplacementAsFailed(ringColor, this.what);
    ringsBeingReplaced.remove(ringColor);
    return 0;
  }

  private void requestRingDependencies(
      CraftingSimulationState sandbox,
      ICraftingService craftingService,
      CraftingPlanCompressedRing ring,
      double scale)
      throws CraftBranchFailure, InterruptedException {
    for (var stack : ring.catalysts()) {
      AEKey key = stack.getKey();
      long amountNeeded = stack.getLongValue();
      if (key.equals(job.getOutput())) {
        long extracted = job.trackRingUsage(key, amountNeeded);
        if (extracted > 0) {
          sandbox.insert(key, extracted, Actionable.MODULATE);
        }
      }
      new AEACraftingTreeNode(craftingService, job, stack.getKey(), stack.getLongValue(), null, -1)
          .request(sandbox, 1, null, false);
    }
    for (var stack : ring.netInputs()) {
      long required = (long) Math.ceil(stack.getLongValue() * scale);
      if (required > 0) {
        new AEACraftingTreeNode(craftingService, job, stack.getKey(), 1, null, -1)
            .request(sandbox, required, null, false);
      }
    }
  }

  private void unpackRingOperations(
      CraftingSimulationState sandbox, CraftingPlanCompressedRing ring, double scale) {
    for (var entry : ring.executionRatio().entrySet()) {
      var pattern = entry.getKey();
      long times = (long) Math.ceil(entry.getValue() * scale);
      if (times > 0) {
        sandbox.addCrafting(pattern, times);
      }
    }
    for (var stack : ring.netOutputs()) {
      long produced = (long) Math.floor(stack.getLongValue() * scale);
      if (produced > 0) {
        sandbox.insert(stack.getKey(), produced, Actionable.MODULATE);
      }
    }
  }
}

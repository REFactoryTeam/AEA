package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ISubMenuHost;
import appeng.core.AELog;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.ref.aea.integration.aea.advancedterminal.AdvancedAutoCraftEntry;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalMode;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalPart;
import com.ref.aea.network.AEANetwork;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.concurrent.Future;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see AdvancedCraftConfirmScreen
 */
public class AdvancedCraftConfirmMenu extends AEBaseMenu implements ISubMenu {

  private static final String ACTION_BACK = "back";
  private static final String ACTION_CYCLE_CPU = "cycleCpu";
  private static final String ACTION_START_JOB = "startJob";
  private static final String ACTION_REPLAN = "replan";

  private static final CraftConfirmMenu.SyncableSubmitResult NO_ERROR =
      new CraftConfirmMenu.SyncableSubmitResult((ICraftingSubmitResult) null);

  public static final MenuType<AdvancedCraftConfirmMenu> TYPE =
      MenuTypeBuilder.create(AdvancedCraftConfirmMenu::new, ISubMenuHost.class)
          .build("advanced_craftconfirm");
  private final AdvancedCraftingCPUCycler cpuCycler;

  private ICraftingCPU selectedCpu;

  private AEKey whatToCraft;
  private long amount;
  private Future<ICraftingPlan> job;
  private ICraftingPlan result;

  @GuiSync(3)
  public boolean autoStart = false;

  // Indicates whether any CPUs are available
  @GuiSync(6)
  public boolean noCPU = true;

  // Properties of the currently selected crafting CPU, this can be null
  // if no CPUs are available, or if an automatic one is selected
  @GuiSync(1)
  public long cpuBytesAvail;

  @GuiSync(2)
  public int cpuCoProcessors;

  @GuiSync(7)
  public Component cpuName;

  @GuiSync(8)
  public CraftConfirmMenu.SyncableSubmitResult submitError = NO_ERROR;

  private CraftingPlanSummary plan;

  private final ISubMenuHost host;

  /**
   * List of stacks to craft, once this request is through. This is currently used when requesting
   * multiple ingredients of a recipe in REI at the same time via ctrl+click.
   *
   * <p>The list is empty if the stacks have been requested via REI and canceling should just return
   * to the screen, null if canceling should return to the craft amount menu.
   */
  @Nullable private List<AdvancedAutoCraftEntry> autoCraftingQueue;

  private IntSet currentTargetSlots;
  private AdvancedTerminalMode mode;

  public AdvancedCraftConfirmMenu(int id, Inventory ip, ISubMenuHost te) {
    super(TYPE, id, ip, te);
    this.host = te;
    this.cpuCycler = new AdvancedCraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
    // A player can select no crafting CPU to use a suitable one automatically
    this.cpuCycler.setAllowNoSelection(true);

    registerClientAction(ACTION_BACK, this::goBack);
    registerClientAction(ACTION_CYCLE_CPU, Boolean.class, this::cycleSelectedCPU);
    registerClientAction(ACTION_START_JOB, this::startJob);
    registerClientAction(ACTION_REPLAN, this::replan);
  }

  /** Open with a list of items to craft, i.e. via REI ctrl+click. */
  public static void openWithCraftingList(
      @Nullable IActionHost terminal,
      ServerPlayer player,
      @Nullable MenuLocator locator,
      List<AdvancedAutoCraftEntry> stacksToCraft,
      AdvancedTerminalMode mode) {
    if (terminal == null || locator == null || stacksToCraft.isEmpty()) {
      return;
    }

    var firstToCraft = stacksToCraft.get(0);
    var subsequentCrafts = stacksToCraft.subList(1, stacksToCraft.size());

    try {
      MenuOpener.open(AdvancedCraftConfirmMenu.TYPE, player, locator);

      if (player.containerMenu instanceof AdvancedCraftConfirmMenu ccc) {
        if (!ccc.planJob(
            firstToCraft.what(), firstToCraft.amount(), CalculationStrategy.CRAFT_LESS)) {
          ccc.setValidMenu(false);
          return;
        }
        ccc.mode = mode;
        ccc.currentTargetSlots = firstToCraft.slot(); // 保存槽位集合
        ccc.autoCraftingQueue = subsequentCrafts;
        ccc.broadcastChanges();
      }
    } catch (Throwable e) {
      AELog.info(e);
    }
  }

  public boolean planJob(AEKey what, long amount, CalculationStrategy strategy) {
    if (this.job != null) {
      this.job.cancel(true);
    }
    this.result = null;
    this.clearError();

    this.whatToCraft = what;
    this.amount = amount;

    var player = getPlayer();

    var grid = getGrid();
    if (grid == null) {
      return false;
    }

    var cg = grid.getCraftingService();

    this.job =
        cg.beginCraftingCalculation(player.level(), this::getActionSrc, what, amount, strategy);
    return true;
  }

  public void cycleSelectedCPU(boolean next) {
    if (isClientSide()) {
      sendClientAction(ACTION_CYCLE_CPU, next);
    } else {
      this.cpuCycler.cycleCpu(next);
    }
  }

  @Override
  public void broadcastChanges() {
    if (isClientSide()) {
      return;
    }

    var grid = this.getGrid();

    // Close the screen if the grid no longer exists
    if (grid == null) {
      this.setValidMenu(false);
      return;
    }

    this.cpuCycler.detectAndSendChanges(grid);

    super.broadcastChanges();

    if (this.job != null && this.job.isDone()) {
      try {
        this.result = this.job.get();

        if (!this.result.simulation() && this.isAutoStart()) {
          this.startJob();
          return;
        }

        this.plan = CraftingPlanSummary.fromJob(getGrid(), getActionSrc(), this.result);

        Player player = this.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
          AdvancedCraftConfirmPlanPacket packet = new AdvancedCraftConfirmPlanPacket(plan);
          AEANetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }

      } catch (Throwable e) {
        this.getPlayerInventory().player.sendSystemMessage(Component.literal("Error: " + e));
        AELog.warn("Failed to start crafting job.", e);
        this.setValidMenu(false);
        this.result = null;
      }

      this.job = null;
    }
  }

  private IGrid getGrid() {
    final IActionHost h = (IActionHost) this.getTarget();
    final IGridNode a = h.getActionableNode();
    return a != null ? a.getGrid() : null;
  }

  private boolean cpuMatches(ICraftingCPU c) {
    if (this.plan == null) {
      return true;
    }
    return c.getAvailableStorage() >= this.plan.getUsedBytes() && !c.isBusy();
  }

  public void startJob() {
    clearError();

    if (isClientSide()) {
      sendClientAction(ACTION_START_JOB);
      return;
    }

    if (this.result != null && !this.result.simulation()) {
      final ICraftingService cc = this.getGrid().getCraftingService();
      if (!(this.host instanceof AdvancedTerminalPart part)) return;
      var submitResult =
          cc.submitJob(this.result, part, this.selectedCpu, true, this.getActionSrc());
      this.setAutoStart(false);
      if (submitResult.successful()) {
        part.addCraftingLink(submitResult.link(), this.currentTargetSlots, mode);
        if (autoCraftingQueue != null && !autoCraftingQueue.isEmpty()) {
          // Process next stack!
          AdvancedCraftConfirmMenu.openWithCraftingList(
              getActionHost(), (ServerPlayer) getPlayer(), getLocator(), autoCraftingQueue, mode);
        } else {
          this.host.returnToMainMenu(getPlayer(), this);
        }
      } else {
        AELog.info(
            "Couldn't submit crafting job for %dx%s: %s [Detail: %s]",
            result.finalOutput().amount(),
            result.finalOutput().what(),
            submitResult.errorCode(),
            submitResult.errorDetail());
        this.submitError = new CraftConfirmMenu.SyncableSubmitResult(submitResult);
      }
    }
  }

  private IActionSource getActionSrc() {
    return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
  }

  @Override
  public void removed(@NotNull Player player) {
    super.removed(player);
    if (this.job != null) {
      this.job.cancel(true);
      this.job = null;
    }
  }

  private void onCPUSelectionChanged(AdvancedCraftingCPURecord cpuRecord, boolean cpusAvailable) {
    noCPU = !cpusAvailable;

    if (cpuRecord == null) {
      cpuBytesAvail = 0;
      cpuCoProcessors = 0;
      cpuName = null;
      selectedCpu = null;
    } else {
      cpuBytesAvail = cpuRecord.getSize();
      cpuCoProcessors = cpuRecord.getProcessors();
      cpuName = cpuRecord.getName();
      selectedCpu = cpuRecord.getCpu();
    }
  }

  public Level getLevel() {
    return this.getPlayerInventory().player.level();
  }

  public boolean isAutoStart() {
    return this.autoStart;
  }

  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  public long getCpuAvailableBytes() {
    return this.cpuBytesAvail;
  }

  public int getCpuCoProcessors() {
    return this.cpuCoProcessors;
  }

  public Component getName() {
    return this.cpuName;
  }

  public boolean hasNoCPU() {
    return this.noCPU;
  }

  public void setJob(Future<ICraftingPlan> job) {
    this.job = job;
  }

  /**
   * @return The summary of the crafting plan. This is null as long as the plan has not yet finished
   *     computing, or it wasn't synced to the client yet.
   */
  @Nullable
  public CraftingPlanSummary getPlan() {
    return this.plan;
  }

  public void setPlan(CraftingPlanSummary plan) {
    this.plan = plan;
  }

  public void goBack() {
    clearError();

    Player player = getPlayerInventory().player;
    if (player instanceof ServerPlayer serverPlayer) {
      if (autoCraftingQueue != null && !autoCraftingQueue.isEmpty()) {
        // Process next stack!
        AdvancedCraftConfirmMenu.openWithCraftingList(
            getActionHost(), (ServerPlayer) getPlayer(), getLocator(), autoCraftingQueue, mode);
      } else if (whatToCraft != null) {
        AdvancedCraftAmountMenu.open(serverPlayer, getLocator(), whatToCraft, amount);
      } else {
        // Go back to host menu
        this.host.returnToMainMenu(getPlayer(), this);
      }
    } else {
      sendClientAction(ACTION_BACK);
    }
  }

  @Override
  public ISubMenuHost getHost() {
    return host;
  }

  public void replan() {
    clearError();

    if (isClientSide()) {
      sendClientAction(ACTION_REPLAN);
      return;
    }

    if (whatToCraft != null) {
      if (!planJob(whatToCraft, amount, CalculationStrategy.CRAFT_LESS)) {
        goBack();
      }
    } else {
      goBack();
    }
  }

  public void clearError() {
    this.submitError = NO_ERROR;
  }
}

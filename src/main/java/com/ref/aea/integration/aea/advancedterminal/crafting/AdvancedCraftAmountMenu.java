package com.ref.aea.integration.aea.advancedterminal.crafting;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.ref.aea.network.AEANetwork;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @see AdvancedCraftAmountScreen
 */
public class AdvancedCraftAmountMenu extends AEBaseMenu implements ISubMenu {

  public static final MenuType<AdvancedCraftAmountMenu> TYPE =
      MenuTypeBuilder.create(AdvancedCraftAmountMenu::new, ISubMenuHost.class)
          .build("advanced_craftamount");

  /**
   * This slot is used to synchronize a visual representation of what is to be crafted to the
   * client.
   */
  private final AppEngSlot craftingItem;

  /** This item (server-only) indicates what should actually be crafted. */
  private AEKey whatToCraft;

  private final ISubMenuHost host;

  public AdvancedCraftAmountMenu(int id, Inventory ip, ISubMenuHost host) {
    super(TYPE, id, ip, host);
    this.host = host;
    this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
    this.craftingItem.setHideAmount(true);
    this.addSlot(this.craftingItem, SlotSemantics.MACHINE_OUTPUT);
  }

  @Override
  public ISubMenuHost getHost() {
    return host;
  }

  /** Opens the craft amount screen for the given player. */
  public static void open(
      ServerPlayer player, MenuLocator locator, AEKey whatToCraft, long initialAmount) {
    MenuOpener.open(AdvancedCraftAmountMenu.TYPE, player, locator);

    if (player.containerMenu instanceof AdvancedCraftAmountMenu cca) {
      cca.setWhatToCraft(whatToCraft, initialAmount);
      cca.broadcastChanges();
    }
  }

  public Level getLevel() {
    return this.getPlayerInventory().player.level();
  }

  private void setWhatToCraft(AEKey whatToCraft, long initialAmount) {
    this.whatToCraft = Objects.requireNonNull(whatToCraft, "whatToCraft");
    this.craftingItem.set(GenericStack.wrapInItemStack(whatToCraft, initialAmount));
  }

  /**
   * Confirms the craft request. If called client-side, automatically sends a packet to the server
   * to perform the action there instead.
   *
   * @param amount The number of items to craft.
   * @param craftMissingAmount Craft only as much as needed to have <code>amount</code>
   * @param autoStart Start crafting immediately when the planning is done.
   */
  public void confirm(long amount, boolean craftMissingAmount, boolean autoStart) {
    if (!isServerSide()) {
      AEANetwork.INSTANCE.sendToServer(
          new AdvancedConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart));
      return;
    }

    if (this.whatToCraft == null) {
      return;
    }

    if (craftMissingAmount) {
      var host = getActionHost();
      if (host != null) {
        var node = host.getActionableNode();
        if (node != null) {
          var storage = node.getGrid().getStorageService();
          var existingAmount =
              (int) Math.min(storage.getCachedInventory().get(whatToCraft), Integer.MAX_VALUE);
          if (existingAmount > amount) {
            amount = 0;
          } else {
            amount -= existingAmount;
          }
        }
      }
    }

    var locator = getLocator();
    if (locator != null) {
      var player = getPlayer();
      if (amount > 0) {
        MenuOpener.open(AdvancedCraftConfirmMenu.TYPE, player, locator);

        if (player.containerMenu instanceof AdvancedCraftConfirmMenu ccc) {
          ccc.setAutoStart(autoStart);
          ccc.planJob(whatToCraft, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
          broadcastChanges();
        }
      } else {
        // When the amount to craft is 0, return to the previous menu without crafting
        this.host.returnToMainMenu(player, this);
      }
    }
  }

  @Nullable
  public GenericStack getWhatToCraft() {
    return GenericStack.unwrapItemStack(craftingItem.getItem());
  }
}

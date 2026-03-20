package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.collect.ImmutableSet;
import com.ref.aea.AEA;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class AdvancedTerminalPart extends AbstractTerminalPart implements ICraftingRequester {

  @PartModels
  public static final ResourceLocation MODEL_OFF =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/advanced_terminal_off");

  @PartModels
  public static final ResourceLocation MODEL_ON =
      ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "part/advanced_terminal_on");

  public static final IPartModel MODELS_OFF =
      new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
  public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
  public static final IPartModel MODELS_HAS_CHANNEL =
      new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

  private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);
  private final ConfigInventory processingGrid =
      ConfigInventory.storage(81, this::onStorageChanged);
  private final Map<ICraftingLink, ActiveLinkDetails> activeLinks = new HashMap<>();

  private AdvancedTerminalMode mode = AdvancedTerminalMode.CRAFTING;
  private boolean substitutions = false;
  private boolean fluidSubstitutions = false;
  private IActionSource actionSource;
  private final List<Runnable> gridChangeListeners = new CopyOnWriteArrayList<>();

  public AdvancedTerminalPart(IPartItem<?> partItem) {
    super(partItem);
    getMainNode().addService(ICraftingRequester.class, this);
    this.processingGrid.useRegisteredCapacities();
  }

  protected IActionSource getActionSource() {
    if (this.actionSource == null) this.actionSource = new MachineSource(this);
    return this.actionSource;
  }

  @Override
  public void onChangeInventory(InternalInventory inv, int slot) {
    if (inv == this.craftingGrid) this.gridChangeListeners.forEach(Runnable::run);
    super.onChangeInventory(inv, slot);
  }

  @Override
  public void readFromNBT(CompoundTag data) {
    super.readFromNBT(data);
    this.craftingGrid.readFromNBT(data, "craftingGrid");
    this.processingGrid.readFromChildTag(data, "processingGrid");
    this.activeLinks.clear();
    if (data.contains("activeLinks", Tag.TAG_LIST)) {
      ListTag list = data.getList("activeLinks", Tag.TAG_COMPOUND);
      for (int i = 0; i < list.size(); i++) {
        CompoundTag entry = list.getCompound(i);
        ICraftingLink link = StorageHelper.loadCraftingLink(entry.getCompound("link"), this);
        IntSet slots = new IntRBTreeSet(entry.getIntArray("slots"));
        AdvancedTerminalMode linkMode = AdvancedTerminalMode.values()[entry.getInt("linkMode")];
        this.activeLinks.put(link, new ActiveLinkDetails(slots, linkMode));
      }
    }
    this.mode = AdvancedTerminalMode.values()[data.getInt("termMode")];
    this.substitutions = data.getBoolean("substitutions");
    this.fluidSubstitutions = data.getBoolean("fluidSubstitutions");
  }

  @Override
  public void writeToNBT(CompoundTag data) {
    super.writeToNBT(data);
    this.craftingGrid.writeToNBT(data, "craftingGrid");
    this.processingGrid.writeToChildTag(data, "processingGrid");
    if (!activeLinks.isEmpty()) {
      ListTag list = new ListTag();
      activeLinks.forEach(
          (link, details) -> {
            CompoundTag entry = new CompoundTag();
            CompoundTag linkTag = new CompoundTag();
            link.writeToNBT(linkTag);
            entry.put("link", linkTag);
            entry.putIntArray("slots", details.slots().toIntArray());
            entry.putInt("linkMode", details.mode().ordinal());
            list.add(entry);
          });
      data.put("activeLinks", list);
    }
    data.putInt("termMode", mode.ordinal());
    data.putBoolean("substitutions", substitutions);
    data.putBoolean("fluidSubstitutions", fluidSubstitutions);
  }

  public void addCraftingLink(ICraftingLink link, IntSet slots, AdvancedTerminalMode mode) {
    this.activeLinks.put(link, new ActiveLinkDetails(slots, mode));
    this.markForSave();
  }

  @Override
  public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable actMode) {
    long remaining = amount;
    ActiveLinkDetails details = this.activeLinks.get(link);

    if (details == null) {
      return getInventory() != null
          ? getInventory().insert(what, amount, actMode, getActionSource())
          : 0;
    }

    if (details.mode() == AdvancedTerminalMode.CRAFTING && what instanceof AEItemKey itemKey) {
      for (int slot : details.slots()) {
        if (slot >= 0 && slot < this.craftingGrid.size()) {
          remaining = insertIntoCraftingSlot(this.craftingGrid, slot, itemKey, remaining, actMode);
          if (remaining <= 0) break;
        }
      }
    } else if (details.mode() == AdvancedTerminalMode.PROCESSING) {
      remaining -= this.processingGrid.insert(what, remaining, actMode, getActionSource());
    }

    if (remaining > 0 && getInventory() != null) {
      long networkInserted = getInventory().insert(what, remaining, actMode, getActionSource());
      remaining -= networkInserted;
    }

    return amount - remaining;
  }

  private long insertIntoCraftingSlot(
      AppEngInternalInventory grid, int slot, AEItemKey what, long amount, Actionable actMode) {
    ItemStack stackInSlot = grid.getStackInSlot(slot);
    if (stackInSlot.isEmpty()) {
      long toAdd = Math.min(1, amount);
      if (toAdd > 0) {
        if (actMode == Actionable.MODULATE) grid.setItemDirect(slot, what.toStack((int) toAdd));
        return amount - toAdd;
      }
    }
    return amount;
  }

  @Override
  public void jobStateChange(ICraftingLink link) {
    this.activeLinks.remove(link);
    this.markForSave();
  }

  @Override
  public ImmutableSet<ICraftingLink> getRequestedJobs() {
    return ImmutableSet.copyOf(this.activeLinks.keySet());
  }

  @Override
  public MenuType<?> getMenuType(Player p) {
    return AdvancedTerminalMenu.TYPE;
  }

  public void markForSave() {
    getHost().markForSave();
  }

  private void onStorageChanged() {
    markForSave();
  }

  public AdvancedTerminalMode getMode() {
    return mode;
  }

  public void setMode(AdvancedTerminalMode mode) {
    this.mode = mode;
    markForSave();
  }

  public boolean isSubstitutions() {
    return substitutions;
  }

  public void setSubstitutions(boolean s) {
    this.substitutions = s;
    markForSave();
  }

  public boolean isFluidSubstitutions() {
    return fluidSubstitutions;
  }

  public void setFluidSubstitutions(boolean f) {
    this.fluidSubstitutions = f;
    markForSave();
  }

  public AppEngInternalInventory getCraftingGrid() {
    return craftingGrid;
  }

  public ConfigInventory getProcessingGrid() {
    return processingGrid;
  }

  public void addGridListener(Runnable listener) {
    this.gridChangeListeners.add(listener);
  }

  public void removeGridListener(Runnable listener) {
    this.gridChangeListeners.remove(listener);
  }

  public Map<ICraftingLink, ActiveLinkDetails> getActiveLinks() {
    return activeLinks;
  }

  public record ActiveLinkDetails(IntSet slots, AdvancedTerminalMode mode) {}

  @Override
  public IPartModel getStaticModels() {
    return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
  }
}

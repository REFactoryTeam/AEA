package com.ref.aea.integration.aea.advancedterminal;

import static appeng.integration.modules.jeirei.TransferHelper.*;

import appeng.api.stacks.*;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jei.JEIPlugin;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.common.GridInventoryEntry;
import com.ref.aea.network.AEANetwork;
import java.util.*;
import java.util.stream.Collectors;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

public class AdvancedRecipeTransferHandler
    implements IUniversalRecipeTransferHandler<AdvancedTerminalMenu> {

  private final IRecipeTransferHandlerHelper helper;
  private static final int MAX_CRAFTING_SLOTS = 9;
  private static final int MAX_PROCESSING_SLOTS = 81;

  public AdvancedRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
    this.helper = helper;
  }

  @Override
  public @NotNull Class<AdvancedTerminalMenu> getContainerClass() {
    return AdvancedTerminalMenu.class;
  }

  @Override
  public @NotNull Optional<MenuType<AdvancedTerminalMenu>> getMenuType() {
    return Optional.of(AdvancedTerminalMenu.TYPE);
  }

  @Override
  public IRecipeTransferError transferRecipe(
      @NotNull AdvancedTerminalMenu menu,
      @NotNull Object recipe,
      @NotNull IRecipeSlotsView display,
      @NotNull Player player,
      boolean maxTransfer,
      boolean doTransfer) {

    boolean isCraftingRecipe =
        (recipe instanceof CraftingRecipe cr) && cr.canCraftInDimensions(3, 3);
    AdvancedTerminalMode targetMode =
        isCraftingRecipe ? AdvancedTerminalMode.CRAFTING : AdvancedTerminalMode.PROCESSING;
    List<List<GenericStack>> inputs = GenericEntryStackHelper.ofInputs(display);

    if (targetMode == AdvancedTerminalMode.PROCESSING && inputs.size() > MAX_PROCESSING_SLOTS) {
      return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
    }

    if (menu.mode != targetMode) menu.setMode(targetMode);

    boolean craftMissing = net.minecraft.client.gui.screens.Screen.hasControlDown();
    MissingIngredientSlots missingSlots = findMissingIngredients(menu, player, inputs, targetMode);
    int requiredIngredients = (int) inputs.stream().filter(l -> !l.isEmpty()).count();

    if (missingSlots.missingSlots.size() == requiredIngredients && requiredIngredients > 0) {
      List<IRecipeSlotView> inputSlotViews = display.getSlotViews(RecipeIngredientRole.INPUT);
      List<IRecipeSlotView> missingViews =
          missingSlots.missingSlots.stream()
              .map(idx -> idx < inputSlotViews.size() ? inputSlotViews.get(idx) : null)
              .filter(Objects::nonNull)
              .toList();
      return helper.createUserErrorForMissingSlots(ItemModText.NO_ITEMS.text(), missingViews);
    }

    if (doTransfer) {
      ResourceLocation recipeId = null;
      if (isCraftingRecipe) {
        Recipe<?> r = (Recipe<?>) recipe;
        recipeId =
            player.level().getRecipeManager().byKey(r.getId()).isPresent() ? r.getId() : null;
      }

      List<GenericStack> templates = buildTransferTemplates(menu, player, inputs, targetMode);
      AEANetwork.INSTANCE.sendToServer(
          new AdvancedFillGridPacket(targetMode, recipeId, templates, craftMissing));
      return null;
    }

    if (missingSlots.anyMissingOrCraftable()) {
      int color = missingSlots.anyMissing() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
      return new ErrorRenderer(missingSlots, craftMissing, color);
    }

    return null;
  }

  private List<GenericStack> buildTransferTemplates(
      AdvancedTerminalMenu menu,
      Player player,
      List<List<GenericStack>> inputs,
      AdvancedTerminalMode targetMode) {
    List<GenericStack> templates = new ArrayList<>();
    Map<AEKey, Long> availableCounts = getAvailableCounts(menu, player);
    Set<AEKey> availableKeys = availableKeys(menu);
    Set<AEKey> craftableKeys = craftableKeys(menu);

    if (targetMode == AdvancedTerminalMode.CRAFTING) {
      for (int i = 0; i < inputs.size() && i < MAX_CRAFTING_SLOTS; i++) {
        List<GenericStack> alternatives = inputs.get(i);
        if (alternatives.isEmpty()) {
          templates.add(null);
          continue;
        }

        GenericStack stackToUse =
            alternatives.stream()
                .filter(
                    gs ->
                        gs.what() instanceof AEItemKey ik
                            && (availableKeys.contains(ik)
                                || hasItemInInventory(player, ik.toStack())
                                || hasItemInCraftingGrid(menu, ik)))
                .findFirst()
                .orElseGet(
                    () ->
                        alternatives.stream()
                            .filter(gs -> craftableKeys.contains(gs.what()))
                            .findFirst()
                            .orElse(alternatives.get(0)));

        templates.add(stackToUse);
      }
    } else {
      for (List<GenericStack> alternatives : inputs) {
        if (alternatives.isEmpty()) continue;

        long totalNeeded = alternatives.get(0).amount();
        long remaining = totalNeeded;

        Optional<GenericStack> fullyAvailable =
            alternatives.stream()
                .filter(alt -> availableCounts.getOrDefault(alt.what(), 0L) >= totalNeeded)
                .findFirst();

        if (fullyAvailable.isPresent()) {
          AEKey key = fullyAvailable.get().what();
          addOrMerge(templates, new GenericStack(key, totalNeeded));
          availableCounts.put(key, availableCounts.get(key) - totalNeeded);
          remaining = 0;
        } else {
          for (GenericStack alt : alternatives) {
            AEKey key = alt.what();
            long available = availableCounts.getOrDefault(key, 0L);
            if (available > 0) {
              long toTake = Math.min(remaining, available);
              addOrMerge(templates, new GenericStack(key, toTake));
              availableCounts.put(key, available - toTake);
              remaining -= toTake;
            }
            if (remaining <= 0) break;
          }
        }
        if (remaining > 0) {
          Optional<GenericStack> craftable =
              alternatives.stream().filter(gs -> craftableKeys.contains(gs.what())).findFirst();

          if (craftable.isPresent()) {
            addOrMerge(templates, new GenericStack(craftable.get().what(), remaining));
            remaining = 0;
          }
        }
        if (remaining > 0) {
          addOrMerge(templates, new GenericStack(alternatives.get(0).what(), remaining));
        }
      }
    }
    return templates;
  }

  private Map<AEKey, Long> getAvailableCounts(AdvancedTerminalMenu menu, Player player) {
    Map<AEKey, Long> counts = new HashMap<>();
    if (menu.getClientRepo() != null) {
      menu.getClientRepo().getAllEntries().stream()
          .filter(e -> e.getStoredAmount() > 0)
          .forEach(e -> counts.put(e.getWhat(), e.getStoredAmount()));
    }
    countPlayerInventory(player, counts);
    return counts;
  }

  private MissingIngredientSlots findMissingIngredients(
      AdvancedTerminalMenu menu,
      Player player,
      List<List<GenericStack>> inputs,
      AdvancedTerminalMode mode) {
    Set<Integer> missing = new HashSet<>();
    Set<Integer> craftable = new HashSet<>();
    Map<AEKey, Long> availableCounts = getAvailableCounts(menu, player);

    if (mode == AdvancedTerminalMode.CRAFTING) {
      for (int i = 0; i < menu.getCraftingMatrix().size(); i++) {
        ItemStack gridStack = menu.getCraftingMatrix().getStackInSlot(i);
        if (!gridStack.isEmpty()) {
          availableCounts.merge(AEItemKey.of(gridStack), (long) gridStack.getCount(), Long::sum);
        }
      }
    }

    Set<AEKey> craftableKeys = craftableKeys(menu);

    for (int i = 0; i < inputs.size(); i++) {
      List<GenericStack> alternatives = inputs.get(i);
      if (alternatives.isEmpty()) continue;

      boolean satisfied = false;
      for (GenericStack gs : alternatives) {
        long needed = (mode == AdvancedTerminalMode.CRAFTING) ? 1 : gs.amount();
        long available = availableCounts.getOrDefault(gs.what(), 0L);
        if (available >= needed) {
          availableCounts.put(gs.what(), available - needed);
          satisfied = true;
          break;
        }
      }

      if (!satisfied) {
        boolean canBeCrafted =
            alternatives.stream().anyMatch(gs -> craftableKeys.contains(gs.what()));
        if (canBeCrafted) craftable.add(i);
        else missing.add(i);
      }
    }

    return new MissingIngredientSlots(missing, craftable);
  }

  protected void countPlayerInventory(Player player, Map<AEKey, Long> availableCounts) {
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack invStack = player.getInventory().getItem(i);
      if (!invStack.isEmpty()) {
        availableCounts.merge(AEItemKey.of(invStack), (long) invStack.getCount(), Long::sum);
      }
    }
  }

  private Set<AEKey> availableKeys(AdvancedTerminalMenu menu) {
    if (menu.getClientRepo() == null) return Collections.emptySet();
    return menu.getClientRepo().getAllEntries().stream()
        .filter(e -> e.getStoredAmount() > 0)
        .map(GridInventoryEntry::getWhat)
        .collect(Collectors.toSet());
  }

  private Set<AEKey> craftableKeys(AdvancedTerminalMenu menu) {
    if (menu.getClientRepo() == null) return Collections.emptySet();
    return menu.getClientRepo().getAllEntries().stream()
        .filter(GridInventoryEntry::isCraftable)
        .map(GridInventoryEntry::getWhat)
        .collect(Collectors.toSet());
  }

  protected boolean hasItemInInventory(Player player, ItemStack stack) {
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      if (ItemStack.isSameItemSameTags(player.getInventory().getItem(i), stack)) return true;
    }
    return false;
  }

  private boolean hasItemInCraftingGrid(AdvancedTerminalMenu menu, AEKey key) {
    for (int i = 0; i < menu.getCraftingMatrix().size(); i++) {
      if (key.equals(AEItemKey.of(menu.getCraftingMatrix().getStackInSlot(i)))) return true;
    }
    return false;
  }

  private void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
    for (int i = 0; i < stacks.size(); i++) {
      if (Objects.equals(stacks.get(i).what(), newStack.what())) {
        stacks.set(
            i, new GenericStack(newStack.what(), stacks.get(i).amount() + newStack.amount()));
        return;
      }
    }
    stacks.add(newStack);
  }

  public record MissingIngredientSlots(Set<Integer> missingSlots, Set<Integer> craftableSlots) {
    public boolean anyMissingOrCraftable() {
      return !missingSlots.isEmpty() || !craftableSlots.isEmpty();
    }

    public boolean anyMissing() {
      return !missingSlots.isEmpty();
    }
  }

  private record ErrorRenderer(MissingIngredientSlots indices, boolean craftMissing, int color)
      implements IRecipeTransferError {
    @Override
    public @NotNull Type getType() {
      return Type.COSMETIC;
    }

    @Override
    public int getButtonHighlightColor() {
      return color;
    }

    @Override
    public void showError(
        @NotNull GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        @NotNull IRecipeSlotsView slots,
        int recipeX,
        int recipeY) {
      var poseStack = guiGraphics.pose();
      poseStack.pushPose();
      poseStack.translate(recipeX, recipeY, 0);

      var slotViews = slots.getSlotViews(RecipeIngredientRole.INPUT);
      for (int i = 0; i < slotViews.size(); i++) {
        boolean missing = indices.missingSlots().contains(i);
        boolean craftable = indices.craftableSlots().contains(i);
        if (missing || craftable) {
          slotViews
              .get(i)
              .drawHighlight(
                  guiGraphics, missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
        }
      }
      poseStack.popPose();

      var tooltip =
          TransferHelper.createCraftingTooltip(
              new appeng.menu.me.items.CraftingTermMenu.MissingIngredientSlots(
                  indices.missingSlots(), indices.craftableSlots()),
              craftMissing);
      JEIPlugin.drawHoveringText(guiGraphics, tooltip, mouseX, mouseY);
    }
  }
}

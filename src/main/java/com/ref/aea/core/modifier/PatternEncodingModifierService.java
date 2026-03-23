package com.ref.aea.core.modifier;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import com.google.common.math.LongMath;
import com.ref.aea.api.client.PatternEncodingModifier;
import java.util.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PatternEncodingModifierService {

  static Set<PatternEncodingModifier<?>> Modifiers = new HashSet<>();

  public static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR =
      Comparator.comparing(GridInventoryEntry::isCraftable)
          .thenComparing(PatternEncodingModifierService::isUndamaged)
          .thenComparing(GridInventoryEntry::getStoredAmount);

  private static Boolean isUndamaged(GridInventoryEntry entry) {
    return !(entry.getWhat() instanceof AEItemKey itemKey) || !itemKey.isDamaged();
  }

  public static void register(PatternEncodingModifier<?> modifier) {
    Modifiers.add(modifier);
  }

  public static void encode(
      PatternEncodingTermMenu menu,
      Object recipeBase,
      IRecipeSlotsView slotsView,
      Player player,
      boolean maxTransfer) {
    List<List<GenericStack>> inputs = GenericEntryStackHelper.ofInputs(slotsView);
    List<GenericStack> outputs = GenericEntryStackHelper.ofOutputs(slotsView);

    PatternEncodingModifier.ModificationResult modificationResult =
        new PatternEncodingModifier.ModificationResult(
            inputs, outputs, PatternEncodingModifier.MergeMode.GLOBAL);

    PatternEncodingModifier.ModificationContext modificationContext =
        new PatternEncodingModifier.ModificationContext(recipeBase, slotsView, player, maxTransfer);

    for (var modifier : Modifiers) {
      modificationResult = getModificationResult(modificationResult, modificationContext, modifier);
    }

    modificationResult =
        getModificationResult(modificationResult, modificationContext, GlobalModifier.INSTANCE);

    if (modificationResult.mergeMode() == PatternEncodingModifier.MergeMode.GLOBAL) {
      EncodingHelper.encodeProcessingRecipe(
          menu, modificationResult.inputs(), modificationResult.outputs());
    } else {
      PatternEncodingModifierService.encodeProcessingRecipeCustom(
          menu,
          modificationResult.inputs(),
          modificationResult.outputs(),
          modificationResult.mergeMode());
    }
  }

  private static PatternEncodingModifier.@NotNull ModificationResult getModificationResult(
      PatternEncodingModifier.ModificationResult modificationResult,
      PatternEncodingModifier.ModificationContext modificationContext,
      PatternEncodingModifier<?> modifier) {
    var beforeGlobalMode = modificationResult.mergeMode();
    var afterGlobalResult = modifier.modify(modificationResult, modificationContext);

    if (afterGlobalResult.mergeMode().ordinal() < beforeGlobalMode.ordinal()) {
      modificationResult =
          new PatternEncodingModifier.ModificationResult(
              afterGlobalResult.inputs(), afterGlobalResult.outputs(), beforeGlobalMode);
    } else {
      modificationResult = afterGlobalResult;
    }
    return modificationResult;
  }

  public static void encodeProcessingRecipeCustom(
      PatternEncodingTermMenu menu,
      List<List<GenericStack>> genericIngredients,
      List<GenericStack> genericResults,
      PatternEncodingModifier.MergeMode mode) {

    menu.setMode(EncodingMode.PROCESSING);
    Map<AEKey, Integer> ingredientPriorities =
        EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);

    encodeBestMatchingStacksIntoSlots(
        genericIngredients, ingredientPriorities, menu.getProcessingInputSlots(), mode);
    encodeBestMatchingStacksIntoSlots(
        genericResults.stream().map(List::of).toList(),
        ingredientPriorities,
        menu.getProcessingOutputSlots(),
        mode);
  }

  private static void encodeBestMatchingStacksIntoSlots(
      List<List<GenericStack>> possibleInputsBySlot,
      Map<AEKey, Integer> ingredientPriorities,
      FakeSlot[] slots,
      PatternEncodingModifier.MergeMode mode) {

    ArrayList<GenericStack> encodedInputs = new ArrayList<>();

    for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
      if (!genericIngredient.isEmpty()) {
        addStack(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient), mode);
      }
    }

    for (int i = 0; i < slots.length; ++i) {
      FakeSlot slot = slots[i];
      ItemStack stack =
          i < encodedInputs.size()
              ? GenericStack.wrapInItemStack(encodedInputs.get(i))
              : ItemStack.EMPTY;
      NetworkHandler.instance()
          .sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, stack));
    }
  }

  private static GenericStack findBestIngredient(
      Map<AEKey, Integer> ingredientPriorities, List<GenericStack> possibleIngredients) {
    return possibleIngredients.stream()
        .map(gi -> Pair.of(gi, ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE)))
        .max(Comparator.comparingInt(Pair::getRight))
        .map(Pair::getLeft)
        .orElseThrow();
  }

  private static void addStack(
      List<GenericStack> stacks, GenericStack newStack, PatternEncodingModifier.MergeMode mode) {
    if (mode == PatternEncodingModifier.MergeMode.ADJACENT && !stacks.isEmpty()) {
      int lastIndex = stacks.size() - 1;
      GenericStack lastStack = stacks.get(lastIndex);

      if (Objects.equals(lastStack.what(), newStack.what())) {
        long newAmount = LongMath.saturatedAdd(lastStack.amount(), newStack.amount());
        stacks.set(lastIndex, new GenericStack(newStack.what(), newAmount));

        long overflow = newStack.amount() - (newAmount - lastStack.amount());
        if (overflow > 0L) {
          stacks.add(new GenericStack(newStack.what(), overflow));
        }

        return;
      }
    }
    stacks.add(newStack);
  }
}

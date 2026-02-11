package com.ref.aea.core.modifier;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import com.google.common.math.LongMath;
import com.ref.aea.api.modifier.PatternEncodingModifier;
import java.util.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

@OnlyIn(Dist.CLIENT)
public class PatternEncodingModifierService {

  static Set<PatternEncodingModifier<?>> Modifiers = new HashSet<>();

  static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR =
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
        new PatternEncodingModifier.ModificationResult(inputs, outputs, false);
    PatternEncodingModifier.ModificationContext modificationContext =
        new PatternEncodingModifier.ModificationContext(recipeBase, slotsView, player, maxTransfer);

    for (var modifier : Modifiers) {
      modificationResult = modifier.modify(modificationResult, modificationContext);
    }
    modificationResult = GlobalModifier.INSTANCE.modify(modificationResult, modificationContext);

    if (modificationResult.mergeAdjacently()) {
      PatternEncodingModifierService.encodeProcessingRecipeAdjacentMerge(
          menu, modificationResult.inputs(), modificationResult.outputs());
    } else {
      EncodingHelper.encodeProcessingRecipe(
          menu, modificationResult.inputs(), modificationResult.outputs());
    }
  }

  public static void encodeProcessingRecipeAdjacentMerge(
      PatternEncodingTermMenu menu,
      List<List<GenericStack>> genericIngredients,
      List<GenericStack> genericResults) {
    menu.setMode(EncodingMode.PROCESSING);
    Map<AEKey, Integer> ingredientPriorities =
        EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
    encodeBestMatchingStacksIntoSlotsAdjacent(
        genericIngredients, ingredientPriorities, menu.getProcessingInputSlots());
    encodeBestMatchingStacksIntoSlotsAdjacent(
        genericResults.stream().map(List::of).toList(),
        ingredientPriorities,
        menu.getProcessingOutputSlots());
  }

  private static void encodeBestMatchingStacksIntoSlotsAdjacent(
      List<List<GenericStack>> possibleInputsBySlot,
      Map<AEKey, Integer> ingredientPriorities,
      FakeSlot[] slots) {
    ArrayList<GenericStack> encodedInputs = new ArrayList<>();

    for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
      if (!genericIngredient.isEmpty()) {
        addOrMergeAdjacent(
            encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient));
      }
    }

    for (int i = 0; i < slots.length; ++i) {
      FakeSlot slot = slots[i];
      ItemStack stack =
          i < encodedInputs.size()
              ? GenericStack.wrapInItemStack(encodedInputs.get(i))
              : ItemStack.EMPTY;
      slot.setFilterTo(stack);
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

  private static void addOrMergeAdjacent(List<GenericStack> stacks, GenericStack newStack) {
    if (!stacks.isEmpty()) {
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

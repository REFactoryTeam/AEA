package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.stacks.AEKey;
import appeng.helpers.IMenuCraftingPacket;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Represents an entry for an advanced auto-crafting request.
 *
 * @param what The key of the item/fluid to be crafted.
 * @param amount The requested amount.
 * @param slot The slots in the grid associated with this crafting request.
 */
public record AdvancedAutoCraftEntry(AEKey what, long amount, IntSet slot) {
  public static AdvancedAutoCraftEntry fromAutoCraftEntry(
      IMenuCraftingPacket.AutoCraftEntry entry) {
    return new AdvancedAutoCraftEntry(
        entry.what(), entry.slots().size(), new IntOpenHashSet(entry.slots()));
  }
}

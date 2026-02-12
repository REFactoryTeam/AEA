package com.ref.aea.util;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class EncodedPatternUtil {
  public static ItemStack fromBuildList(
      ServerLevel level, ServerPlayer player, ArrayList<StatePos> buildList, String templateName) {
    if (buildList == null || buildList.isEmpty()) return ItemStack.EMPTY;
    Map<AEKey, Long> counts = getAeKeyLongMap(level, player, buildList);

    if (counts.isEmpty()) return ItemStack.EMPTY;

    List<GenericStack> inputs =
        counts.entrySet().stream()
            .map(e -> new GenericStack(e.getKey(), e.getValue()))
            .sorted((a, b) -> Long.compare(b.amount(), a.amount()))
            .limit(81)
            .toList();

    ItemStack barrierStack = new ItemStack(Registration.Template.get());
    if (templateName != null && !templateName.isBlank()) {
      barrierStack.setHoverName(Component.literal(templateName));
    }

    AEKey outputKey = AEItemKey.of(barrierStack);
    if (outputKey == null) return ItemStack.EMPTY;

    GenericStack[] outputs = new GenericStack[] {new GenericStack(outputKey, 1)};

    return AEItems.PROCESSING_PATTERN.asItem().encode(inputs.toArray(new GenericStack[0]), outputs);
  }

  private static @NotNull Map<AEKey, Long> getAeKeyLongMap(
      ServerLevel level, ServerPlayer player, ArrayList<StatePos> buildList) {
    Map<AEKey, Long> counts = new HashMap<>();
    Map<BlockState, Map<AEKey, Long>> cacheMap = new HashMap<>();
    for (StatePos sp : buildList) {
      if (sp.state.isAir()) continue;
      Map<AEKey, Long> requirementsForState =
          cacheMap.computeIfAbsent(
              sp.state,
              state -> {
                Map<AEKey, Long> singleStateReqs = new HashMap<>();
                if (!state.getFluidState().isEmpty() && state.getFluidState().isSource()) {
                  AEKey key = AEFluidKey.of(state.getFluidState().getType());
                  singleStateReqs.put(key, 1000L);
                } else {
                  List<ItemStack> drops =
                      GadgetUtils.getDropsForBlockState(level, BlockPos.ZERO, state, player);
                  for (ItemStack req : drops) {
                    AEKey key = AEItemKey.of(req);
                    if (key != null) {
                      singleStateReqs.merge(key, (long) req.getCount(), Long::sum);
                    }
                  }
                }
                return singleStateReqs;
              });
      requirementsForState.forEach((key, amount) -> counts.merge(key, amount, Long::sum));
    }
    return counts;
  }
}

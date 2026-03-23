package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class AdvancedFillProcessingGridPacket {

  private final List<GenericStack> exactRequests;
  private final boolean craftMissing;

  public AdvancedFillProcessingGridPacket(List<GenericStack> exactRequests, boolean craftMissing) {
    this.exactRequests = exactRequests;
    this.craftMissing = craftMissing;
  }

  public static void encode(AdvancedFillProcessingGridPacket msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.exactRequests.size());
    for (GenericStack stack : msg.exactRequests) {
      GenericStack.writeBuffer(stack, buf);
    }
    buf.writeBoolean(msg.craftMissing);
  }

  public static AdvancedFillProcessingGridPacket decode(FriendlyByteBuf buf) {
    int size = buf.readInt();
    List<GenericStack> requests = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      requests.add(GenericStack.readBuffer(buf));
    }
    boolean craftMissing = buf.readBoolean();
    return new AdvancedFillProcessingGridPacket(requests, craftMissing);
  }

  public static void handle(
      AdvancedFillProcessingGridPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(
        () -> {
          ServerPlayer player = ctx.getSender();
          if (player == null || !(player.containerMenu instanceof AdvancedTerminalMenu menu))
            return;

          var node = menu.getNetworkNode();
          if (node == null) return;

          var grid = node.getGrid();
          var storage = grid.getStorageService().getInventory();
          var energy = grid.getEnergyService();
          var craftingService = grid.getCraftingService();
          var processingGrid = menu.getProcessingGrid();

          Map<AEKey, Long> consolidatedRequests = new LinkedHashMap<>();
          for (GenericStack req : msg.exactRequests) {
            consolidatedRequests.merge(
                req.what(), req.amount() <= 0 ? 1L : req.amount(), Long::sum);
          }

          boolean touched = false;
          List<AdvancedAutoCraftEntry> toAutoCraft = new ArrayList<>();

          for (Map.Entry<AEKey, Long> entry : consolidatedRequests.entrySet()) {
            AEKey exactKey = entry.getKey();
            long reqAmount = entry.getValue();

            long extracted =
                StorageHelper.poweredExtraction(
                    energy, storage, exactKey, reqAmount, menu.getActionSource());

            if (extracted < reqAmount && exactKey instanceof AEItemKey itemKey) {
              long needed = reqAmount - extracted;
              var playerInv = player.getInventory();
              for (int i = 0; i < playerInv.items.size() && needed > 0; i++) {
                if (menu.isPlayerInventorySlotLocked(i)) continue;
                ItemStack invStack = playerInv.getItem(i);
                if (!invStack.isEmpty() && itemKey.matches(invStack)) {
                  int toTake = (int) Math.min(needed, invStack.getCount());
                  invStack.shrink(toTake);
                  extracted += toTake;
                  needed -= toTake;
                }
              }
            }

            if (extracted > 0) {
              long inserted =
                  processingGrid.insert(
                      exactKey, extracted, Actionable.MODULATE, menu.getActionSource());
              if (inserted > 0) touched = true;

              long overflow = extracted - inserted;
              if (overflow > 0) {
                long back =
                    StorageHelper.poweredInsert(
                        energy, storage, exactKey, overflow, menu.getActionSource());
                if (overflow - back > 0 && exactKey instanceof AEItemKey itemKey) {
                  player
                      .getInventory()
                      .placeItemBackInInventory(itemKey.toStack((int) (overflow - back)));
                }
              }
            }

            long missing = reqAmount - extracted;
            if (missing > 0 && msg.craftMissing && craftingService.isCraftable(exactKey)) {
              toAutoCraft.add(new AdvancedAutoCraftEntry(exactKey, missing, new IntRBTreeSet()));
            }
          }

          if (!toAutoCraft.isEmpty()) {
            if (touched) grid.getStorageService().invalidateCache();
            menu.startAdvancedAutoCrafting(toAutoCraft, AdvancedTerminalMode.PROCESSING);
          }
        });
    ctx.setPacketHandled(true);
  }
}

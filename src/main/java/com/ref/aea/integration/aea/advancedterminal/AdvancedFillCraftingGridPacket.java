package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.helpers.IMenuCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.util.CraftingRecipeUtil;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class AdvancedFillCraftingGridPacket {
  @Nullable private final ResourceLocation recipeId;
  private final NonNullList<ItemStack> ingredientTemplates;
  private final boolean craftMissing;

  public AdvancedFillCraftingGridPacket(
      @Nullable ResourceLocation recipeId,
      NonNullList<ItemStack> ingredientTemplates,
      boolean craftMissing) {
    this.recipeId = recipeId;
    this.ingredientTemplates = ingredientTemplates;
    this.craftMissing = craftMissing;
  }

  public static void encode(AdvancedFillCraftingGridPacket msg, FriendlyByteBuf buf) {
    if (msg.recipeId != null) {
      buf.writeBoolean(true);
      buf.writeResourceLocation(msg.recipeId);
    } else {
      buf.writeBoolean(false);
    }
    buf.writeInt(msg.ingredientTemplates.size());
    for (ItemStack stack : msg.ingredientTemplates) buf.writeItem(stack);
    buf.writeBoolean(msg.craftMissing);
  }

  public static AdvancedFillCraftingGridPacket decode(FriendlyByteBuf buf) {
    ResourceLocation recipeId = buf.readBoolean() ? buf.readResourceLocation() : null;
    int size = buf.readInt();
    NonNullList<ItemStack> templates = NonNullList.withSize(size, ItemStack.EMPTY);
    for (int i = 0; i < size; i++) templates.set(i, buf.readItem());
    boolean craftMissing = buf.readBoolean();
    return new AdvancedFillCraftingGridPacket(recipeId, templates, craftMissing);
  }

  public static void handle(
      AdvancedFillCraftingGridPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(
        () -> {
          ServerPlayer player = ctx.getSender();
          if (player == null || !(player.containerMenu instanceof IMenuCraftingPacket cct)) return;
          if (!cct.useRealItems()) return;

          var node = cct.getNetworkNode();
          if (node == null) return;

          var grid = node.getGrid();
          var storageService = grid.getStorageService();
          var energy = grid.getEnergyService();
          var craftMatrix = cct.getCraftingMatrix();
          var storage = storageService.getInventory();
          var cachedStorage = storageService.getCachedInventory();
          var filter = ViewCellItem.createItemFilter(cct.getViewCells());
          var ingredients = getDesiredIngredients(msg, player);
          var craftingService = grid.getCraftingService();
          var toAutoCraft = new LinkedHashMap<AEItemKey, List<Integer>>();
          boolean touchedGridStorage = false;

          for (int x = 0; x < craftMatrix.size(); x++) {
            var currentItem = craftMatrix.getStackInSlot(x);
            var ingredient = ingredients.get(x);

            if (!currentItem.isEmpty()) {
              if (ingredient.test(currentItem)) continue;
              var in = AEItemKey.of(currentItem);
              var inserted =
                  StorageHelper.poweredInsert(
                      energy, storage, in, currentItem.getCount(), cct.getActionSource());
              if (inserted > 0) touchedGridStorage = true;
              if (inserted < currentItem.getCount()) {
                currentItem = currentItem.copy();
                currentItem.shrink((int) inserted);
              } else {
                currentItem = ItemStack.EMPTY;
              }
              player.getInventory().add(currentItem);
              craftMatrix.setItemDirect(x, currentItem.isEmpty() ? ItemStack.EMPTY : currentItem);
            }

            if (ingredient.isEmpty()) continue;

            if (currentItem.isEmpty()) {
              var request = findBestMatchingItemStack(ingredient, filter, cachedStorage);
              for (var what : request) {
                var extracted =
                    StorageHelper.poweredExtraction(
                        energy, storage, what, 1, cct.getActionSource());
                if (extracted > 0) {
                  touchedGridStorage = true;
                  currentItem = what.toStack(Ints.saturatedCast(extracted));
                  break;
                }
              }
            }

            if (currentItem.isEmpty()) {
              currentItem = takeIngredientFromPlayer(cct, player, ingredient);
            }

            craftMatrix.setItemDirect(x, currentItem);

            if (currentItem.isEmpty() && msg.craftMissing) {
              int slot = x;
              findCraftableKey(ingredient, craftingService)
                  .ifPresent(
                      key -> {
                        toAutoCraft.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
                      });
            }
          }

          player.containerMenu.slotsChanged(craftMatrix.toContainer());

          if (!toAutoCraft.isEmpty()) {
            if (touchedGridStorage) storageService.invalidateCache();
            var stacks =
                toAutoCraft.entrySet().stream()
                    .map(e -> new IMenuCraftingPacket.AutoCraftEntry(e.getKey(), e.getValue()))
                    .toList();
            cct.startAutoCrafting(stacks);
          }
        });
    ctx.setPacketHandled(true);
  }

  private static NonNullList<Ingredient> getDesiredIngredients(
      AdvancedFillCraftingGridPacket msg, Player player) {
    if (msg.recipeId != null) {
      var recipe = player.level().getRecipeManager().byKey(msg.recipeId).orElse(null);
      if (recipe != null) return CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
    }
    var ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
    Preconditions.checkArgument(ingredients.size() == msg.ingredientTemplates.size());
    for (int i = 0; i < ingredients.size(); i++) {
      if (!msg.ingredientTemplates.get(i).isEmpty()) {
        ingredients.set(i, Ingredient.of(msg.ingredientTemplates.get(i)));
      }
    }
    return ingredients;
  }

  private static ItemStack takeIngredientFromPlayer(
      IMenuCraftingPacket cct, ServerPlayer player, Ingredient ingredient) {
    var playerInv = player.getInventory();
    for (int i = 0; i < playerInv.items.size(); i++) {
      if (cct.isPlayerInventorySlotLocked(i)) continue;
      var item = playerInv.getItem(i);
      if (ingredient.test(item)) {
        var result = item.split(1);
        if (!result.isEmpty()) return result;
      }
    }
    return ItemStack.EMPTY;
  }

  private static List<AEItemKey> findBestMatchingItemStack(
      Ingredient ingredient, IPartitionList filter, KeyCounter storage) {
    return Arrays.stream(ingredient.getItems())
        .map(AEItemKey::of)
        .filter(r -> r != null && (filter == null || filter.isListed(r)))
        .flatMap(s -> storage.findFuzzy(s, FuzzyMode.IGNORE_ALL).stream())
        .filter(e -> ((AEItemKey) e.getKey()).matches(ingredient))
        .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
        .map(e -> (AEItemKey) e.getKey())
        .toList();
  }

  private static Optional<AEItemKey> findCraftableKey(
      Ingredient ingredient, ICraftingService craftingService) {
    return Arrays.stream(ingredient.getItems())
        .map(AEItemKey::of)
        .map(
            s ->
                (AEItemKey)
                    craftingService.getFuzzyCraftable(
                        s, key -> ((AEItemKey) key).matches(ingredient)))
        .filter(Objects::nonNull)
        .findAny();
  }
}

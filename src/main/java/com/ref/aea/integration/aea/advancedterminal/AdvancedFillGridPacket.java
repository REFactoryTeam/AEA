package com.ref.aea.integration.aea.advancedterminal;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.*;
import appeng.api.storage.StorageHelper;
import appeng.items.storage.ViewCellItem;
import appeng.util.CraftingRecipeUtil;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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

public class AdvancedFillGridPacket {

  private final AdvancedTerminalMode mode;
  @Nullable private final ResourceLocation recipeId;
  private final List<GenericStack> ingredientTemplates;
  private final boolean craftMissing;

  public AdvancedFillGridPacket(
      AdvancedTerminalMode targetMode,
      @Nullable ResourceLocation recipeId,
      List<GenericStack> ingredientTemplates,
      boolean craftMissing) {
    this.mode = targetMode;
    this.recipeId = recipeId;
    this.ingredientTemplates = ingredientTemplates;
    this.craftMissing = craftMissing;
  }

  public static void encode(AdvancedFillGridPacket msg, FriendlyByteBuf buffer) {
    buffer.writeEnum(msg.mode);
    if (msg.recipeId != null) {
      buffer.writeBoolean(true);
      buffer.writeResourceLocation(msg.recipeId);
    } else {
      buffer.writeBoolean(false);
    }

    buffer.writeInt(msg.ingredientTemplates.size());
    for (var stack : msg.ingredientTemplates) {
      if (stack != null && stack.what() != null) {
        buffer.writeBoolean(true);
        GenericStack.writeBuffer(stack, buffer);
      } else {
        buffer.writeBoolean(false);
      }
    }
    buffer.writeBoolean(msg.craftMissing);
  }

  public static AdvancedFillGridPacket decode(FriendlyByteBuf buffer) {
    AdvancedTerminalMode mode = buffer.readEnum(AdvancedTerminalMode.class);
    ResourceLocation recipeId = buffer.readBoolean() ? buffer.readResourceLocation() : null;

    int size = buffer.readInt();
    List<GenericStack> ingredientTemplates = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      if (buffer.readBoolean()) {
        ingredientTemplates.add(GenericStack.readBuffer(buffer));
      } else {
        ingredientTemplates.add(null);
      }
    }
    boolean craftMissing = buffer.readBoolean();
    return new AdvancedFillGridPacket(mode, recipeId, ingredientTemplates, craftMissing);
  }

  public static void handle(AdvancedFillGridPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
    NetworkEvent.Context ctx = ctxGetter.get();
    ctx.enqueueWork(() -> processPacket(msg, ctx.getSender()));
    ctx.setPacketHandled(true);
  }

  private static void processPacket(AdvancedFillGridPacket msg, ServerPlayer player) {
    if (player == null || !(player.containerMenu instanceof AdvancedTerminalMenu advancedMenu))
      return;

    var node = advancedMenu.getNetworkNode();
    if (node == null) return;

    List<AdvancedAutoCraftEntry> toAutoCraft = new ArrayList<>();
    boolean touchedGridStorage;

    if (msg.mode == AdvancedTerminalMode.CRAFTING) {
      touchedGridStorage = handleCraftingMode(msg, player, advancedMenu, toAutoCraft, node);
    } else {
      touchedGridStorage = handleProcessingMode(msg, player, advancedMenu, toAutoCraft, node);
    }

    if (!toAutoCraft.isEmpty()) {
      if (touchedGridStorage) {
        node.getGrid().getStorageService().invalidateCache();
      }
      consolidateAndStartCrafting(advancedMenu, toAutoCraft, msg.mode);
    }
  }

  private static boolean handleCraftingMode(
      AdvancedFillGridPacket msg,
      ServerPlayer player,
      AdvancedTerminalMenu advancedMenu,
      List<AdvancedAutoCraftEntry> toAutoCraft,
      IGridNode node) {
    boolean touched = false;
    var grid = node.getGrid();
    var storageService = grid.getStorageService();
    var energy = grid.getEnergyService();
    var craftingService = grid.getCraftingService();
    var storage = storageService.getInventory();
    var craftMatrix = advancedMenu.getCraftingMatrix();
    var cachedStorage = storageService.getCachedInventory();
    IPartitionList filter = ViewCellItem.createItemFilter(advancedMenu.getViewCells());

    var ingredients = getDesiredIngredients(player, msg.recipeId, msg.ingredientTemplates);

    for (int i = 0; i < craftMatrix.size() && i < ingredients.size(); i++) {
      var ingredient = ingredients.get(i);
      var currentItem = craftMatrix.getStackInSlot(i);

      if (!currentItem.isEmpty()) {
        if (ingredient.test(currentItem)) continue;

        var in = AEItemKey.of(currentItem);
        var inserted =
            StorageHelper.poweredInsert(
                energy,
                storage,
                Objects.requireNonNull(in),
                currentItem.getCount(),
                advancedMenu.getActionSource());
        if (inserted > 0) touched = true;

        if (inserted < currentItem.getCount()) {
          currentItem = currentItem.copy();
          currentItem.shrink((int) inserted);
          player.getInventory().add(currentItem);
        }
        currentItem = ItemStack.EMPTY;
        craftMatrix.setItemDirect(i, ItemStack.EMPTY);
      }

      if (ingredient.isEmpty()) continue;

      if (currentItem.isEmpty()) {
        var request = findBestMatchingItemStack(ingredient, filter, cachedStorage);
        for (var what : request) {
          var extracted =
              StorageHelper.poweredExtraction(
                  energy, storage, what, 1, advancedMenu.getActionSource());
          if (extracted > 0) {
            touched = true;
            currentItem = what.toStack(1);
            break;
          }
        }
      }

      if (currentItem.isEmpty()) {
        currentItem = takeExactIngredientFromPlayer(advancedMenu, player, ingredient);
      }

      craftMatrix.setItemDirect(i, currentItem);

      if (currentItem.isEmpty() && msg.craftMissing) {
        Optional<AEItemKey> craftableItem = findCraftableItemKey(ingredient, craftingService);
        if (craftableItem.isPresent()) {
          toAutoCraft.add(new AdvancedAutoCraftEntry(craftableItem.get(), 1, IntSet.of(i)));
        } else if (advancedMenu.fluidSubstitutions) {
          for (ItemStack stack : ingredient.getItems()) {
            GenericStack fluidStack =
                ContainerItemStrategies.getContainedStack(stack, AEKeyType.fluids());
            if (fluidStack != null && craftingService.isCraftable(fluidStack.what())) {
              toAutoCraft.add(
                  new AdvancedAutoCraftEntry(fluidStack.what(), fluidStack.amount(), IntSet.of(i)));
              break;
            }
          }
        }
      }
    }
    advancedMenu.slotsChanged(craftMatrix.toContainer());
    return touched;
  }

  private static boolean handleProcessingMode(
      AdvancedFillGridPacket msg,
      ServerPlayer player,
      AdvancedTerminalMenu advancedMenu,
      List<AdvancedAutoCraftEntry> toAutoCraft,
      IGridNode node) {
    boolean touched = false;
    var grid = node.getGrid();
    var storage = grid.getStorageService().getInventory();
    var energy = grid.getEnergyService();
    var craftingService = grid.getCraftingService();
    var processingGrid = advancedMenu.getProcessingGrid();

    for (var template : msg.ingredientTemplates) {
      if (template == null || template.what() == null) continue;

      AEKey key = template.what();
      long reqAmount = template.amount() > 0 ? template.amount() : 1;

      long extracted =
          StorageHelper.poweredExtraction(
              energy, storage, key, reqAmount, advancedMenu.getActionSource());
      if (extracted < reqAmount && key instanceof AEItemKey itemKey) {
        int needed = (int) Math.min(Integer.MAX_VALUE, reqAmount - extracted);
        ItemStack taken =
            takeMultipleIngredientsFromPlayer(
                advancedMenu, player, Ingredient.of(itemKey.toStack()), needed);
        if (!taken.isEmpty()) extracted += taken.getCount();
      }

      if (extracted > 0) {
        long insertedToGrid =
            processingGrid.insert(
                key, extracted, Actionable.MODULATE, advancedMenu.getActionSource());
        if (insertedToGrid > 0) touched = true;

        long overflow = extracted - insertedToGrid;
        if (overflow > 0) {
          long insertedBack =
              StorageHelper.poweredInsert(
                  energy, storage, key, overflow, advancedMenu.getActionSource());
          long stillRemaining = overflow - insertedBack;
          if (stillRemaining > 0 && key instanceof AEItemKey itemKey) {
            player.getInventory().placeItemBackInInventory(itemKey.toStack((int) stillRemaining));
          }
        }
      }

      long missing = reqAmount - extracted;
      if (missing > 0 && msg.craftMissing && craftingService.isCraftable(key)) {
        toAutoCraft.add(new AdvancedAutoCraftEntry(key, missing, IntSet.of()));
      }
    }
    return touched;
  }

  private static void consolidateAndStartCrafting(
      AdvancedTerminalMenu menu,
      List<AdvancedAutoCraftEntry> toAutoCraft,
      AdvancedTerminalMode mode) {
    Map<AEKey, Long> mergedAmounts = new LinkedHashMap<>();
    Map<AEKey, IntSet> mergedSlots = new HashMap<>();

    for (var entry : toAutoCraft) {
      mergedAmounts.merge(entry.what(), entry.amount(), Long::sum);
      mergedSlots.computeIfAbsent(entry.what(), k -> new IntRBTreeSet()).addAll(entry.slot());
    }

    List<AdvancedAutoCraftEntry> consolidated = new ArrayList<>();
    mergedAmounts.forEach(
        (key, amount) ->
            consolidated.add(new AdvancedAutoCraftEntry(key, amount, mergedSlots.get(key))));

    menu.startAdvancedAutoCrafting(consolidated, mode);
  }

  private static NonNullList<Ingredient> getDesiredIngredients(
      Player player, ResourceLocation recipeId, List<GenericStack> ingredientTemplates) {
    if (recipeId != null) {
      var recipe = player.level().getRecipeManager().byKey(recipeId).orElse(null);
      if (recipe != null) {
        return CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
      }
    }
    var ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
    for (int i = 0; i < 9 && i < ingredientTemplates.size(); i++) {
      var template = ingredientTemplates.get(i);
      if (template != null && template.what() instanceof AEItemKey itemKey) {
        ingredients.set(i, Ingredient.of(itemKey.toStack()));
      }
    }
    return ingredients;
  }

  protected static ItemStack takeExactIngredientFromPlayer(
      AdvancedTerminalMenu menu, ServerPlayer player, Ingredient ingredient) {
    var playerInv = player.getInventory();
    for (int i = 0; i < playerInv.items.size(); i++) {
      if (menu.isPlayerInventorySlotLocked(i)) continue;
      var item = playerInv.getItem(i);
      if (ingredient.test(item)) {
        var result = item.split(1);
        if (!result.isEmpty()) return result;
      }
    }
    return ItemStack.EMPTY;
  }

  protected static ItemStack takeMultipleIngredientsFromPlayer(
      AdvancedTerminalMenu menu, ServerPlayer player, Ingredient ingredient, int needed) {
    var playerInv = player.getInventory();
    ItemStack takenStack = ItemStack.EMPTY;
    for (int i = 0; i < playerInv.items.size() && needed > 0; i++) {
      if (menu.isPlayerInventorySlotLocked(i)) continue;
      var item = playerInv.getItem(i);
      if (!item.isEmpty() && ingredient.test(item)) {
        if (takenStack.isEmpty()) {
          takenStack = item.copy();
          takenStack.setCount(0);
        } else if (!ItemStack.isSameItemSameTags(takenStack, item)) {
          continue;
        }
        int toTake = Math.min(needed, item.getCount());
        item.shrink(toTake);
        takenStack.grow(toTake);
        needed -= toTake;
      }
    }
    return takenStack;
  }

  private static Optional<AEItemKey> findCraftableItemKey(
      Ingredient ingredient, ICraftingService craftingService) {
    return Arrays.stream(ingredient.getItems())
        .map(AEItemKey::of)
        .map(
            s ->
                (AEItemKey)
                    craftingService.getFuzzyCraftable(
                        s, key -> key instanceof AEItemKey itemKey && itemKey.matches(ingredient)))
        .filter(Objects::nonNull)
        .findAny();
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
}

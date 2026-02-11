package com.ref.aea.core.color;

import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.hooks.ticking.TickHandler;
import appeng.me.service.helpers.NetworkCraftingProviders;
import com.google.common.collect.Iterators;
import com.ref.aea.api.mixin.ae.crafting.color.CraftingPlanCompressedRing;
import com.ref.aea.api.mixin.ae.crafting.color.IMixinPatternDetails;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Keeps track of the crafting patterns in the network, and related information. */
public class AEANetworkCraftingProviders extends NetworkCraftingProviders {
  /** Tracks the provider state for each grid node that provides auto-crafting to the network. */
  private final Map<IGridNode, ProviderState> craftingProviders = new HashMap<>();

  /**
   * Tracks state for crafting providers that may be provided without a grid node, such as by other
   * grid services.
   */
  private final List<ProviderState> globalProviders = new ArrayList<>();

  private final Map<IPatternDetails, CraftingProviderList> craftingMethods = new HashMap<>();
  private final Map<AEKey, PatternsForKey> craftableItems = new HashMap<>();
  private final Map<Integer, Set<IPatternDetails>> patternsByColor = new HashMap<>();
  private final Map<Integer, CraftingPlanCompressedRing> compressedRingCache = new HashMap<>();

  /** Used for looking up craftable alternatives using fuzzy search (i.e. ignore NBT). */
  private final KeyCounter craftableItemsList = new KeyCounter();

  private final Map<AEKey, Integer> emitableItems = new HashMap<>();

  private final Set<AEKey> craftableKeys = Collections.unmodifiableSet(craftableItems.keySet());
  private final Set<AEKey> emittableKeys = Collections.unmodifiableSet(emitableItems.keySet());

  private long lastModifiedOnTick = TickHandler.instance().getCurrentTick();

  @Override
  public void addProvider(IGridNode node) {
    var provider = node.getService(ICraftingProvider.class);
    if (provider != null) {
      if (craftingProviders.containsKey(node)) {
        throw new IllegalArgumentException(
            "Duplicate crafting provider registration for node " + node);
      }
      var state = new ProviderState(provider);
      state.mount(this);
      craftingProviders.put(node, state);
      setLastModifiedOnTick();
    }
  }

  public void addProvider(ICraftingProvider provider) {
    for (var state : globalProviders) {
      if (state.provider == provider) {
        throw new IllegalArgumentException(
            "Duplicate crafting provider registration for " + provider);
      }
    }

    var state = new ProviderState(provider);
    state.mount(this);
    globalProviders.add(state);
    setLastModifiedOnTick();
  }

  @Override
  public void removeProvider(IGridNode node) {
    var provider = node.getService(ICraftingProvider.class);
    if (provider != null) {
      var state = craftingProviders.remove(node);
      if (state != null) {
        state.unmount(this);
        setLastModifiedOnTick();
      }
    }
  }

  public void removeProvider(ICraftingProvider provider) {
    var it = this.globalProviders.iterator();
    while (it.hasNext()) {
      var state = it.next();
      if (state.provider == provider) {
        it.remove();
        state.unmount(this);
        setLastModifiedOnTick();
      }
    }
  }

  @Override
  public Set<AEKey> getCraftables(AEKeyFilter filter) {
    var result = new HashSet<AEKey>();

    // add craftable items!
    for (var stack : this.craftableItems.keySet()) {
      if (filter.matches(stack)) {
        result.add(stack);
      }
    }

    for (var stack : this.emitableItems.keySet()) {
      if (filter.matches(stack)) {
        result.add(stack);
      }
    }

    return result;
  }

  @Override
  public Set<AEKey> getCraftableKeys() {
    return craftableKeys;
  }

  @Override
  public Set<AEKey> getEmittableKeys() {
    return emittableKeys;
  }

  @Override
  public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
    var patterns = this.craftableItems.get(whatToCraft);
    if (patterns != null) {
      return patterns.getSortedPatterns(); // The result of Stream.toList() is already unmodifiable
    }
    return Collections.emptyList();
  }

  @Override
  @Nullable
  public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
    for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
      if (filter.matches(fuzzy.getKey())) {
        return fuzzy.getKey();
      }
    }
    return null;
  }

  @Override
  public boolean canEmitFor(AEKey someItem) {
    return this.emitableItems.containsKey(someItem);
  }

  @Override
  public Iterable<ICraftingProvider> getMediums(IPatternDetails key) {
    var mediumList = this.craftingMethods.get(key);
    return Objects.requireNonNullElse(mediumList, Collections.emptyList());
  }

  private void setLastModifiedOnTick() {
    lastModifiedOnTick = TickHandler.instance().getCurrentTick();
    this.compressedRingCache.clear();
  }

  /**
   * @see TickHandler#getCurrentTick()
   */
  @Override
  public long getLastModifiedOnTick() {
    return lastModifiedOnTick;
  }

  private static class CraftingProviderList implements Iterable<ICraftingProvider> {
    private final List<ICraftingProvider> providers = new ArrayList<>();

    /**
     * Cycling iterator for round-robin. Has to be refreshed after every addition or removal to
     * providers to prevent CMEs.
     */
    private Iterator<ICraftingProvider> cycleIterator = Iterators.cycle(providers);

    private void add(ICraftingProvider provider) {
      providers.add(provider);
      cycleIterator = Iterators.cycle(providers);
    }

    private void remove(ICraftingProvider provider) {
      providers.remove(provider);
      cycleIterator = Iterators.cycle(providers);
    }

    @Override
    public @NotNull Iterator<ICraftingProvider> iterator() {
      return Iterators.limit(cycleIterator, providers.size());
    }
  }

  private static class ProviderState {
    private final ICraftingProvider provider;
    private final Set<AEKey> emitableItems;
    private final List<IPatternDetails> patterns;
    private final int priority;

    private ProviderState(ICraftingProvider provider) {
      this.provider = provider;
      this.emitableItems = new HashSet<>(provider.getEmitableItems());
      this.patterns = new ArrayList<>(provider.getAvailablePatterns());
      this.priority = provider.getPatternPriority();
    }

    private void mount(AEANetworkCraftingProviders methods) {
      for (var emitable : emitableItems) {
        methods.emitableItems.merge(emitable, 1, Integer::sum);
      }
      for (var pattern : patterns) {
        // output -> pattern (for simulation)
        var primaryOutput = pattern.getPrimaryOutput();

        methods.craftableItemsList.add(primaryOutput.what(), 1);

        var patternsForKey =
            methods.craftableItems.computeIfAbsent(primaryOutput.what(), k -> new PatternsForKey());
        patternsForKey.patterns.add(new PatternInfo(pattern, this));
        patternsForKey.needsSorting = true;

        // pattern -> method (for execution)
        methods
            .craftingMethods
            .computeIfAbsent(pattern, d -> new CraftingProviderList())
            .add(provider);

        // color -> pattern (for grouping/filtering)
        int color = ((IMixinPatternDetails) pattern).AEA$getColor();
        methods.patternsByColor.computeIfAbsent(color, k -> new HashSet<>()).add(pattern);
      }
    }

    private void unmount(AEANetworkCraftingProviders methods) {
      for (var emitable : emitableItems) {
        methods.emitableItems.compute(emitable, (key, cnt) -> cnt == 1 ? null : cnt - 1);
      }
      for (var pattern : patterns) {
        var primaryOutput = pattern.getPrimaryOutput();

        methods.craftableItemsList.remove(primaryOutput.what(), 1);

        methods.craftableItems.computeIfPresent(
            primaryOutput.what(),
            (key, patternsForKey) -> {
              patternsForKey.patterns.remove(new PatternInfo(pattern, this));
              patternsForKey.needsSorting = true;
              return patternsForKey.patterns.isEmpty() ? null : patternsForKey;
            });

        methods.craftingMethods.computeIfPresent(
            pattern,
            (pat, list) -> {
              list.remove(provider);
              return list.providers.isEmpty() ? null : list;
            });

        int color = ((IMixinPatternDetails) pattern).AEA$getColor();
        methods.patternsByColor.computeIfPresent(
            color,
            (c, set) -> {
              set.remove(pattern);
              return set.isEmpty() ? null : set;
            });
      }
    }
  }

  private static class PatternsForKey {
    private final Set<PatternInfo> patterns = new HashSet<>();
    private List<IPatternDetails> sortedPatterns = Collections.emptyList();
    private boolean needsSorting = false;

    private void sortPatterns() {
      sortedPatterns =
          patterns.stream()
              .sorted(Comparator.comparingInt((PatternInfo pi) -> pi.state.priority).reversed())
              .map(PatternInfo::pattern)
              .distinct()
              .toList();
    }

    private List<IPatternDetails> getSortedPatterns() {
      if (needsSorting) {
        sortPatterns();
      }
      return sortedPatterns;
    }
  }

  private record PatternInfo(IPatternDetails pattern, ProviderState state) {}

  /**
   * 根据指定的颜色高效地获取所有相关的配方。
   *
   * @param color 颜色值，-1 代表无色或默认分组。
   * @return 一个包含所有匹配配方的不可变集合。如果没有匹配的配方，则返回空集合。
   */
  public Collection<IPatternDetails> getPatternsByColor(int color) {
    var patterns = this.patternsByColor.get(color);
    return patterns != null ? Collections.unmodifiableSet(patterns) : Collections.emptySet();
  }

  /**
   * ADDED: Public accessor for crafting calculations to get cached ring properties. Triggers
   * calculation if not already cached for this network state.
   *
   * @param color The color of the ring to analyze. Must not be -1.
   * @return The compressed ring properties, or null if the ring is invalid, empty, or cannot be
   *     calculated.
   */
  @Nullable
  public CraftingPlanCompressedRing getOrCalculateCompressedRing(int color) {
    if (color == -1) {
      return null;
    }
    return compressedRingCache.computeIfAbsent(
        color, key -> CraftingPlanCompressedRing.calculateAndCacheRingProperties(key, this));
  }
}

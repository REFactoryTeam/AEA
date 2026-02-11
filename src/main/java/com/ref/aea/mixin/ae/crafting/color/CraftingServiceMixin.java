package com.ref.aea.mixin.ae.crafting.color;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingCalculation;
import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;
import com.ref.aea.api.mixin.ae.crafting.color.CraftingPlanCompressedRing;
import com.ref.aea.api.mixin.ae.crafting.color.IMixinCraftingService;
import com.ref.aea.config.AEAServerConfig;
import com.ref.aea.core.color.AEACraftingCalculation;
import com.ref.aea.core.color.AEANetworkCraftingProviders;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftingService.class, remap = false)
public class CraftingServiceMixin implements IMixinCraftingService {

  @Shadow @Final private NetworkCraftingProviders craftingProviders;

  @Shadow @Final private static ExecutorService CRAFTING_POOL;

  @Shadow @Final private IGrid grid;

  @Override
  @Nullable
  public CraftingPlanCompressedRing AEA$getCompressedRing(int color) {
    return ((AEANetworkCraftingProviders) this.craftingProviders)
        .getOrCalculateCompressedRing(color);
  }

  @Redirect(
      method = "<init>",
      at = @At(value = "NEW", target = "()Lappeng/me/service/helpers/NetworkCraftingProviders;"))
  private NetworkCraftingProviders redirectCraftingProviders() {
    return new AEANetworkCraftingProviders();
  }

  /**
   * @author RemakeFactory
   * @reason .
   */
  @Overwrite
  public Future<ICraftingPlan> beginCraftingCalculation(
      Level level,
      ICraftingSimulationRequester simRequester,
      AEKey what,
      long amount,
      CalculationStrategy strategy) {
    if (level == null || simRequester == null) {
      throw new IllegalArgumentException("Invalid Crafting Job Request");
    }
    if (AEAServerConfig.color) {
      final AEACraftingCalculation job =
          new AEACraftingCalculation(
              level, this.grid, simRequester, new GenericStack(what, amount), strategy);
      return CRAFTING_POOL.submit(job::run);

    } else {
      final CraftingCalculation job =
          new CraftingCalculation(
              level, this.grid, simRequester, new GenericStack(what, amount), strategy);
      return CRAFTING_POOL.submit(job::run);
    }
  }
}

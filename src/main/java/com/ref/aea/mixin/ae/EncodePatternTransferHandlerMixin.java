package com.ref.aea.mixin.ae;

import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.ref.aea.config.AEAClientConfig;
import com.ref.aea.core.modifier.PatternEncodingModifierService;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EncodePatternTransferHandler.class)
@OnlyIn(Dist.CLIENT)
public abstract class EncodePatternTransferHandlerMixin {

  @Inject(
      method = "transferRecipe*",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lappeng/integration/modules/jeirei/EncodingHelper;encodeProcessingRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Ljava/util/List;Ljava/util/List;)V"),
      cancellable = true,
      remap = false)
  private void injectProcessingRecipeHook(
      PatternEncodingTermMenu menu,
      Object recipeBase,
      IRecipeSlotsView slotsView,
      Player player,
      boolean maxTransfer,
      boolean doTransfer,
      CallbackInfoReturnable<IRecipeTransferError> cir) {
    if (AEAClientConfig.useModifier) {
      PatternEncodingModifierService.encode(menu, recipeBase, slotsView, player, maxTransfer);
      cir.setReturnValue(null);
    }
  }
}

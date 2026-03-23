package com.ref.aea.integration.jei;

import com.ref.aea.AEA;
import com.ref.aea.integration.aea.advancedterminal.AdvancedCraftingRecipeTransferHandler;
import com.ref.aea.integration.aea.advancedterminal.AdvancedProcessingRecipeTransferHandler;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class AEAJEIPlugin implements IModPlugin {
  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return ResourceLocation.fromNamespaceAndPath(AEA.MOD_ID, "core");
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
        new AdvancedCraftingRecipeTransferHandler<>(
            AdvancedTerminalMenu.class,
            AdvancedTerminalMenu.TYPE,
            registration.getTransferHelper()),
        RecipeTypes.CRAFTING);
    registration.addUniversalRecipeTransferHandler(
        new AdvancedProcessingRecipeTransferHandler(
            registration.getTransferHelper(), registration.getJeiHelpers().getStackHelper()));
  }
}

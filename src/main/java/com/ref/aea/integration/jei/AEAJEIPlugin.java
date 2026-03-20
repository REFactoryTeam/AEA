package com.ref.aea.integration.jei;

import com.ref.aea.AEA;
import com.ref.aea.integration.aea.advancedterminal.AdvancedRecipeTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
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
    registration.addUniversalRecipeTransferHandler(
        new AdvancedRecipeTransferHandler(registration.getTransferHelper()));
  }
}

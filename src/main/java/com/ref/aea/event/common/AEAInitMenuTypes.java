package com.ref.aea.event.common;

import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalMenu;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftAmountMenu;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftConfirmMenu;
import net.minecraft.world.inventory.MenuType;

public class AEAInitMenuTypes {
  public static void init() {
    MenuType<AdvancedTerminalMenu> advancedTerminalMenutype = AdvancedTerminalMenu.TYPE;
    MenuType<AdvancedCraftConfirmMenu> advancedCraftConfirmMenuMenuType =
        AdvancedCraftConfirmMenu.TYPE;
    MenuType<AdvancedCraftAmountMenu> advancedCraftAmountMenuMenuType =
        AdvancedCraftAmountMenu.TYPE;
  }
}

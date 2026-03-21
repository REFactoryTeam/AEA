package com.ref.aea.event.client;

import appeng.init.client.InitScreens;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalMenu;
import com.ref.aea.integration.aea.advancedterminal.AdvancedTerminalScreen;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftAmountMenu;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftAmountScreen;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftConfirmMenu;
import com.ref.aea.integration.aea.advancedterminal.crafting.AdvancedCraftConfirmScreen;

public class AEAInitScreens {
  public static void init() {
    InitScreens.register(
        AdvancedCraftAmountMenu.TYPE, AdvancedCraftAmountScreen::new, "/screens/craft_amount.json");
    InitScreens.register(
        AdvancedCraftConfirmMenu.TYPE,
        AdvancedCraftConfirmScreen::new,
        "/screens/craft_confirm.json");
    InitScreens.register(
        AdvancedTerminalMenu.TYPE,
        AdvancedTerminalScreen::new,
        "/screens/terminals/advanced_terminal.json");
  }
}

package com.ref.aea.api.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ICustomScrollBehavior {

  void onScroll(@NotNull ItemStack stack, @NotNull Player player, double delta);

  default boolean shouldTrigger(Player player) {
    return player.isShiftKeyDown();
  }
}

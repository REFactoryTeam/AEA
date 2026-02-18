package com.ref.aea.core.definitions;

import com.ref.aea.AEA;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class AEATab {

  public static Component NAME_SPECIAL =
      Component.literal("A")
          .withStyle(Style.EMPTY.withColor(6017018))
          .append(Component.literal("E").withStyle(Style.EMPTY.withColor(16100281)))
          .append(Component.literal("A").withStyle(ChatFormatting.WHITE));

  public static final DeferredRegister<CreativeModeTab> DR =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AEA.MOD_ID);

  public static final RegistryObject<CreativeModeTab> AEA_TAB =
      DR.register(
          "aea_tab",
          () ->
              CreativeModeTab.builder()
                  .title(getName())
                  .icon(() -> AEAItems.MIRROR_CONNECTION_TOOL.get().getDefaultInstance())
                  .displayItems(AEATab::Add)
                  .build());

  private static @NotNull Component getName() {
    if (new Random().nextInt(1000) == 0) return NAME_SPECIAL;
    return Component.literal("AEA");
  }

  private static void Add(
      CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
    ADD_TAB_ItemLike.forEach(item -> pOutput.accept(item.get()));
    ADD_TAB_ItemStack.forEach(item -> pOutput.accept(item.get()));
  }

  public static final List<Supplier<? extends ItemLike>> ADD_TAB_ItemLike = new ArrayList<>();

  public static final List<Supplier<ItemStack>> ADD_TAB_ItemStack = new ArrayList<>();
}

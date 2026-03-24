package com.ref.aea.core.definitions;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import com.ref.aea.AEA;
import com.ref.aea.data.AEALangProvider;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AEAItems {
  public static final DeferredRegister<Item> DR =
      DeferredRegister.create(ForgeRegistries.ITEMS, AEA.MOD_ID);

  public static <T extends IPart> RegistryObject<PartItem<T>> createPart(
      String id,
      String englishName,
      Item.Properties properties,
      Class<T> partClass,
      Function<IPartItem<T>, T> factory) {
    PartModels.registerModels(PartModelsHelper.createModels(partClass));
    return item(id, englishName, () -> new PartItem<>(properties, partClass, factory));
  }

  public static <T extends Item> RegistryObject<T> item(
      String id, String englishName, Supplier<T> itemSupplier) {
    return item(id, englishName, itemSupplier, true, true);
  }

  public static <T extends Item> RegistryObject<T> blockItem(
      String id, String englishName, Supplier<T> itemSupplier, boolean isCreativeModeTab) {
    return item(id, englishName, itemSupplier, false, isCreativeModeTab);
  }

  public static <T extends Item> RegistryObject<T> item(
      String id,
      String englishName,
      Supplier<T> itemSupplier,
      boolean isData,
      boolean isCreativeModeTab) {
    var deferredItem = DR.register(id, itemSupplier);
    if (isData) {
      AEALangProvider.Items.put(deferredItem, englishName);
    }
    if (isCreativeModeTab) {
      AEATab.ADD_TAB_ItemLike.add(deferredItem);
    }
    return deferredItem;
  }
}

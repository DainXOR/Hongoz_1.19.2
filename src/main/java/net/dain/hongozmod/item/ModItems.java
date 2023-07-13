package net.dain.hongozmod.item;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HongozMod.MOD_ID);

    public static final RegistryObject<Item> ZHONGO_SPAWN_EGG = ITEMS.register("zhongo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ZHONGO, 0x0d0c800, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HORDEN_SPAWN_EGG = ITEMS.register("horden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HORDEN, 0x0d0c800, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> CROAKTAR_SPAWN_EGG = ITEMS.register("croaktar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.CROAKTAR, 0x0d0c800, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HONZIADE_SPAWN_EGG = ITEMS.register("honziade_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE, 0x0d0c800, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE, 0x0d0c800, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
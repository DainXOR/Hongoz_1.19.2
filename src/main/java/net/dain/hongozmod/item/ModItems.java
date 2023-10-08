package net.dain.hongozmod.item;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HongozMod.MOD_ID);

    public static final RegistryObject<Item> MAGGOT_SPAWN_EGG = ITEMS.register("fungi_maggot_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.MAGGOT, 0x998866, 0x7d00d9,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BEACON_SPAWN_EGG = ITEMS.register("beacon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.BEACON, 0x998866, 0x7d00d9,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> ZHONGO_SPAWN_EGG = ITEMS.register("zhongo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ZHONGO, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HORDEN_SPAWN_EGG = ITEMS.register("horden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HORDEN, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> CROAKTAR_SPAWN_EGG = ITEMS.register("croaktar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.CROAKTAR, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HONZIADE_SPAWN_EGG = ITEMS.register("honziade_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNTER, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HUNTER_TEST_SPAWN_EGG = ITEMS.register("hunter_test_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNTER_TEST, 0xc80000, 0x252525,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HONZIADE_QUEEN_SPAWN_EGG = ITEMS.register("honziade_queen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE_QUEEN, 0xfd2ac8, 0xb8db1f,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> EVO_CROAKTAR_SPAWN_EGG = ITEMS.register("evolved_croaktar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.EVO_CROAKTAR, 0xfd2ac8, 0xb8db1f,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> WOLFRAMIUM_INGOT = ITEMS.register(
            "wolframium_ingot",
            () -> new Item(new Item.Properties()
                    .tab(CreativeModeTab.TAB_MISC)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> RAW_WOLFRAMITE = ITEMS.register(
            "raw_wolframite",
            () -> new Item(new Item.Properties()
                    .tab(CreativeModeTab.TAB_MISC)
                    .rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
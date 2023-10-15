package net.dain.hongozmod.item;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.item.custom.ModShieldItem;
import net.dain.hongozmod.tab.HongozTabs;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HongozMod.MOD_ID);

/* <><><><><><><><><><><><><><><><><><><><><><><><><><> Mobs <><><><><><><><><><><><><><><><><><><><><><><><><><> */

    public static final RegistryObject<Item> MAGGOT_SPAWN_EGG = ITEMS.register("fungi_maggot_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.MAGGOT, 0x998866, 0x7d00d9,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> BEACON_SPAWN_EGG = ITEMS.register("beacon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.BEACON, 0x998866, 0x7d00d9,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> ZHONGO_SPAWN_EGG = ITEMS.register("zhongo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ZHONGO, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> HORDEN_SPAWN_EGG = ITEMS.register("horden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HORDEN, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> CROAKTAR_SPAWN_EGG = ITEMS.register("croaktar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.CROAKTAR, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> HONZIADE_SPAWN_EGG = ITEMS.register("honziade_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> HUNTER_SPAWN_EGG = ITEMS.register("hunter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNTER, 0x00d0c8, 0xd6c3aa,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> HUNTER_TEST_SPAWN_EGG = ITEMS.register("hunter_test_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNTER, 0xffa8a8, 0xd6d6d6,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> HONZIADE_QUEEN_SPAWN_EGG = ITEMS.register("honziade_queen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.HONZIADE_QUEEN, 0xfd2ac8, 0xb8db1f,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    public static final RegistryObject<Item> EVO_CROAKTAR_SPAWN_EGG = ITEMS.register("evolved_croaktar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.EVO_CROAKTAR, 0xfd2ac8, 0xb8db1f,
                    new Item.Properties().tab(HongozTabs.MOBS_TAB)));

    /* <><><><><><><><><><><><><><><><><><><><><><><><><><> Materials <><><><><><><><><><><><><><><><><><><><><><><><><><> */

    public static final RegistryObject<Item> RAW_WOLFRAMITE = ITEMS.register(
            "raw_wolframite",
            () -> new Item(new Item.Properties()
                    .tab(HongozTabs.MATERIALS_TAB)
                    .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMITE_DUST = ITEMS.register(
            "wolframite_dust",
            () -> new Item(new Item.Properties()
                    .tab(HongozTabs.MATERIALS_TAB)
                    .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_INGOT = ITEMS.register(
            "wolframium_ingot",
            () -> new Item(new Item.Properties()
                    .tab(HongozTabs.MATERIALS_TAB)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> RAW_BISMUTH = ITEMS.register(
            "raw_bismuth",
            () -> new Item(new Item.Properties()
                    .tab(HongozTabs.MATERIALS_TAB)
                    .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BISMUTH_INGOT = ITEMS.register(
            "bismuth_ingot",
            () -> new Item(new Item.Properties()
                    .tab(HongozTabs.MATERIALS_TAB)
                    .rarity(Rarity.RARE)));

    /* <><><><><><><><><><><><><><><><><><><><><><><><><><> Tools <><><><><><><><><><><><><><><><><><><><><><><><><><> */

    public static final RegistryObject<Item> WOLFRAMIUM_SWORD = ITEMS.register(
            "wolframium_sword",
            () -> new SwordItem(ModTiers.WOLFRAMIUM, 9, 2.5f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_PICKAXE = ITEMS.register(
            "wolframium_pickaxe",
            () -> new PickaxeItem(ModTiers.WOLFRAMIUM, 8, 1.5f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_AXE = ITEMS.register(
            "wolframium_axe",
            () -> new AxeItem(ModTiers.WOLFRAMIUM, 11, 1.0f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_SHOVEL = ITEMS.register(
            "wolframium_shovel",
            () -> new ShovelItem(ModTiers.WOLFRAMIUM, 5, 2.0f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_HOE = ITEMS.register(
            "wolframium_hoe",
            () -> new HoeItem(ModTiers.WOLFRAMIUM, 3, 2.5f,
                    new Item.Properties().tab(HongozTabs.TOOLS_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> WOLFRAMIUM_GREATSWORD = ITEMS.register(
            "wolframium_greatsword",
            () -> new SwordItem(ModTiers.WOLFRAMIUM, 15, 1.1f,
                    new Item.Properties().tab(HongozTabs.TOOLS_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_WAR_PICK = ITEMS.register(
            "wolframium_war_pick",
            () -> new PickaxeItem(ModTiers.WOLFRAMIUM, 10, 3.5f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_GREAT_AXE = ITEMS.register(
            "wolframium_great_axe",
            () -> new AxeItem(ModTiers.WOLFRAMIUM, 18, 0.9f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WOLFRAMIUM_SCYTHE = ITEMS.register(
            "wolframium_scythe",
            () -> new HoeItem(ModTiers.WOLFRAMIUM, 20, 0.7f,
                    new Item.Properties().tab(HongozTabs.WIP_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> WOLFRAMIUM_SHIELD = ITEMS.register(
            "wolframium_shield",
            () -> new ModShieldItem(ModTiers.WOLFRAMIUM,
                    new Item.Properties()
                    .tab(HongozTabs.WIP_TAB)
                    .rarity(Rarity.RARE)
                    .durability(1 << 15)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
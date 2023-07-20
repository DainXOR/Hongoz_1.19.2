package net.dain.hongozmod.block;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HongozMod.MOD_ID);

    public static final RegistryObject<Block> WOLFRAMITE_ORE = registerBlock("wolframite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(20f)
                    .destroyTime(30f)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(75f)),
            CreativeModeTab.TAB_BUILDING_BLOCKS);

    public static final RegistryObject<Block> DEEPSLATE_WOLFRAMITE_ORE = registerBlock("deepslate_wolframite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(20f)
                    .destroyTime(40f)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(75f)),
            CreativeModeTab.TAB_BUILDING_BLOCKS);

    public static final RegistryObject<Block> WOLFRAMIUM_BLOCK = registerBlock("wolframium_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.HEAVY_METAL)
                    .strength(60f)
                    .destroyTime(50f)
                    .requiresCorrectToolForDrops()
                    .explosionResistance(200f)
            ),
            CreativeModeTab.TAB_BUILDING_BLOCKS);

    private static <T extends Block>RegistryObject<T>registerBlock(String name, Supplier<T> block, CreativeModeTab tab){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);

        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab){
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}

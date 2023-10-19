package net.dain.hongozmod.tab;

import net.dain.hongozmod.block.ModBlocks;
import net.dain.hongozmod.item.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class HongozTabs {
    public static final CreativeModeTab MOBS_TAB = new HongozTab("mobs", () -> ModItems.HUNTER_TEST_SPAWN_EGG.get().getDefaultInstance());
    public static final CreativeModeTab MATERIALS_TAB = new HongozTab("materials", () -> ModItems.BISMUTH_INGOT.get().getDefaultInstance());
    public static final CreativeModeTab TOOLS_TAB = new HongozTab("tools", () -> ModItems.WOLFRAMIUM_GREAT_SWORD.get().getDefaultInstance());
    public static final CreativeModeTab WIP_TAB = new HongozTab("wip");

    private static class HongozTab extends CreativeModeTab {
        private final Supplier<ItemStack> iconSupplier;

        public HongozTab(String label, Supplier<ItemStack> icon) {
            super("hongoz_" + label + "_tab");
            this.iconSupplier = icon;
        }
        public HongozTab(String label){
            this(label, () -> ModBlocks.DEEPSLATE_WOLFRAMITE_ORE.get().asItem().getDefaultInstance());
        }


        @Override
        public @NotNull ItemStack makeIcon() {
            return this.iconSupplier.get();
        }
    }

}

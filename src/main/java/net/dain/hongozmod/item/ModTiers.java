package net.dain.hongozmod.item;

import net.dain.hongozmod.HongozMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public class ModTiers {
    public static Tier WOLFRAMIUM;

    static {
        WOLFRAMIUM = TierSortingRegistry.registerTier(
                new ForgeTier(
                        6,
                        1 << 15,
                        11.0f,
                        9.0f,
                        32,
                        ModTags.Blocks.NEEDS_WOLFRAMIUM_TOOL,
                        () -> Ingredient.of(ModItems.WOLFRAMIUM_INGOT.get())),
                new ResourceLocation(HongozMod.MOD_ID, "wolframium"),
                List.of(Tiers.NETHERITE),
                List.of()
        );
    }
}

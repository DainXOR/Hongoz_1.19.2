package net.dain.hongozmod.world.feature;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.config.ModCommonConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;


public class ModPlacedFeatures {
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, HongozMod.MOD_ID);

    public static final RegistryObject<PlacedFeature> WOLFRAMITE_ORE_PLACED = PLACED_FEATURES.register("wolframite_ore_placed",
            () -> new PlacedFeature(ModConfiguredFeatures.WOLFRAMITE_VEIN.getHolder().get(),
                    commonOrePlacement(4, // ModCommonConfig.WOLFRAMITE_SMALL_VEINS_PER_CHUNK.get(),
                            HeightRangePlacement.triangle(
                                    VerticalAnchor.absolute(-60),
                                    VerticalAnchor.absolute(20)))));

    public static final RegistryObject<PlacedFeature> WOLFRAMITE_ORE_PLACED_LARGE = PLACED_FEATURES.register("wolframite_ore_placed_large",
            () -> new PlacedFeature(ModConfiguredFeatures.WOLFRAMITE_VEIN_LARGE.getHolder().get(),
                    rareOrePlacement(2, //ModCommonConfig.WOLFRAMITE_LARGE_VEINS_PER_CHUNK.get(),
                            HeightRangePlacement.triangle(
                                    VerticalAnchor.absolute(-60),
                                    VerticalAnchor.absolute(-20)))));

    private static List<PlacementModifier> orePlacement(PlacementModifier modifier, PlacementModifier modifier1) {
        return List.of(modifier, InSquarePlacement.spread(), modifier1, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightRange) {
        return orePlacement(CountPlacement.of(pCount), pHeightRange);
    }

    private static List<PlacementModifier> rareOrePlacement(int pChance, PlacementModifier pHeightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(pChance), pHeightRange);
    }

    public static void register(IEventBus eventBus){
        PLACED_FEATURES.register(eventBus);
    }
}

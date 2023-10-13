package net.dain.hongozmod.config;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collection;
import java.util.List;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_ORE_SMALL_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_ORE_LARGE_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_ORE_VEINS_SIZE_SMALL;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_ORE_VEINS_SIZE_LARGE;



    public static final ForgeConfigSpec.ConfigValue<Float> DIFFICULTY_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_ARMOR_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_DAMAGE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> MOBS_IGNORE_SUN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MOBS_SUN_BURN;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOBS_WEAKNESSES;
    public static final ForgeConfigSpec.ConfigValue<Float> MOBS_WEAKNESS_MULTIPLIER;

    static {
        BUILDER.push("Hongoz Configuration");

        // Define configs

        // Minerals configs
        WOLFRAMITE_ORE_SMALL_VEINS_PER_CHUNK = BUILDER
                .comment("Small wolframite ore veins that generate per chunk")
                .define("Wolframite small vein chance", 4);
        WOLFRAMITE_ORE_LARGE_VEINS_PER_CHUNK = BUILDER
                .comment("Large wolframite ore veins that generate per chunk")
                .define("Wolframite large vein chance", 2);

        WOLFRAMITE_ORE_VEINS_SIZE_SMALL = BUILDER
                .comment("Max size of the small wolframite ore veins")
                .define("Wolframite small vein size", 3);
        WOLFRAMITE_ORE_VEINS_SIZE_LARGE = BUILDER
                .comment("Max size of the large wolframite ore veins")
                .define("Wolframite large vein size", 7);

        // Mob configs
        DIFFICULTY_MULTIPLIER = BUILDER
                .comment("Affects the spawn count of the mod enemies, their attribute values, drops amounts, etc.")
                .define("Difficulty Multiplier", 1.0f);

        GLOBAL_HEALTH_MULTIPLIER = BUILDER
                .comment("Health multiplier for all the mod mobs.")
                .define("Health Multiplier", 1.0f);
        GLOBAL_ARMOR_MULTIPLIER = BUILDER
                .comment("Armor multiplier for all the mod mobs.")
                .define("Armor Multiplier", 1.0f);
        GLOBAL_DAMAGE_MULTIPLIER = BUILDER
                .comment("Damage multiplier for all the mod mobs.")
                .define("Damage Multiplier", 1.0f);

        MOBS_IGNORE_SUN = BUILDER
                .comment("Mobs can spawn under sun light.")
                .define("Damage Multiplier", false);
        MOBS_SUN_BURN = BUILDER
                .comment("Mobs can be set on fire under the sun.")
                .define("Damage Multiplier", false);

        MOBS_WEAKNESSES = BUILDER
                .comment("Damage sources that deal extra damage to the mobs.")
                .comment("#  Damage sources in Vanilla: ")
                .defineList("Weaknesses", List.of(
                        DamageSource.IN_FIRE.toString(),
                        DamageSource.ON_FIRE.toString(),
                        DamageSource.LIGHTNING_BOLT.toString(),
                        DamageSource.LAVA.toString(),
                        DamageSource.HOT_FLOOR.toString()), source -> true);
        MOBS_WEAKNESS_MULTIPLIER = BUILDER
                .comment("Extra damage multiplier.")
                .define("Weakness multiplier", 4.0f);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

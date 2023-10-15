package net.dain.hongozmod.config;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_SMALL_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_LARGE_VEINS_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_VEINS_SIZE_SMALL;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOLFRAMITE_VEINS_SIZE_LARGE;


    public static final ForgeConfigSpec.ConfigValue<Float> DIFFICULTY_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_ARMOR_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Float> GLOBAL_DAMAGE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> MOBS_IGNORE_SUN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MOBS_SUN_BURN;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOBS_WEAKNESSES;
    public static final ForgeConfigSpec.ConfigValue<Float> MOBS_WEAKNESS_MULTIPLIER;

    static {
        BUILDER.push("hongoz_common_config");

        // Define configs

        // Minerals configs
        BUILDER.push("minerals");

        WOLFRAMITE_SMALL_VEINS_PER_CHUNK = BUILDER
                .comment("Small wolframite ore veins that generate per chunk.")
                .comment("Generate up to n veins per chunk. [default: 4]")
                .define("Wolframite small vein chance", 4);
        WOLFRAMITE_LARGE_VEINS_PER_CHUNK = BUILDER
                .comment("Large wolframite ore veins that generate per chunk.")
                .comment("Generate once for every n chunks. [default: 2]")
                .define("Wolframite large vein chance", 2);

        WOLFRAMITE_VEINS_SIZE_SMALL = BUILDER
                .comment("Max size of the small wolframite ore veins")
                .define("Wolframite small vein size", 3);
        WOLFRAMITE_VEINS_SIZE_LARGE = BUILDER
                .comment("Max size of the large wolframite ore veins")
                .define("Wolframite large vein size", 7);

        BUILDER.pop();
        // Mob configs
        BUILDER.push("mobs");

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

        List<String> damageSourceNames = List.of(
                DamageSource.ANVIL.getMsgId(),
                "arrow",
                DamageSource.CACTUS.getMsgId(),
                DamageSource.CRAMMING.getMsgId(),
                DamageSource.DRAGON_BREATH.getMsgId(),
                DamageSource.DROWN.getMsgId(),
                DamageSource.DRY_OUT.getMsgId(),
                "explosion.player",
                DamageSource.FALL.getMsgId(),
                DamageSource.FALLING_BLOCK.getMsgId(),
                DamageSource.FALLING_STALACTITE.getMsgId(),
                "fireworks",
                DamageSource.FLY_INTO_WALL.getMsgId(),
                DamageSource.FREEZE.getMsgId(),
                DamageSource.GENERIC.getMsgId(),
                DamageSource.HOT_FLOOR.getMsgId(),
                DamageSource.IN_FIRE.getMsgId(),
                DamageSource.IN_WALL.getMsgId(),
                "indirectMagic",
                DamageSource.LAVA.getMsgId(),
                DamageSource.LIGHTNING_BOLT.getMsgId(),
                DamageSource.MAGIC.getMsgId(),
                "mob",
                DamageSource.ON_FIRE.getMsgId(),
                DamageSource.OUT_OF_WORLD.getMsgId(),
                "player",
                "sonic_boom",
                DamageSource.STARVE.getMsgId(),
                "sting",
                DamageSource.SWEET_BERRY_BUSH.getMsgId(),
                "thorns",
                "thrown",
                "trident",
                DamageSource.WITHER.getMsgId(),
                "witherSkull"
        );

        ForgeConfigSpec.Builder Temp = BUILDER
                .comment("Damage sources that deal extra damage to the mobs.")
                .comment("Damage sources in Vanilla: ");

        for (String name : damageSourceNames) {
            Temp = Temp.comment(name);
        }
        MOBS_WEAKNESSES = Temp.defineList("Weaknesses", List.of(
                        DamageSource.IN_FIRE.getMsgId(),
                        DamageSource.ON_FIRE.getMsgId(),
                        DamageSource.LIGHTNING_BOLT.getMsgId(),
                        DamageSource.LAVA.getMsgId(),
                        DamageSource.HOT_FLOOR.getMsgId()),
                        source -> damageSourceNames.contains((String) source)
                );

        MOBS_WEAKNESS_MULTIPLIER = BUILDER
                .comment("Extra damage multiplier.")
                .define("Weakness multiplier", 4.0f);

        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

package net.dain.hongozmod.config;

import net.dain.hongozmod.item.ModItems;
import net.dain.hongozmod.material.ModTiers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ModServerConfig {
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

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOBS_DAMAGE_SOURCE_WEAKNESSES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOBS_MATERIALS_WEAKNESSES;
    public static final ForgeConfigSpec.ConfigValue<Float> MOBS_WEAKNESS_MULTIPLIER;

    static {
        // Values
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

        List<String> defaultDamageSources = List.of(
                DamageSource.IN_FIRE.getMsgId(),
                DamageSource.ON_FIRE.getMsgId(),
                DamageSource.LIGHTNING_BOLT.getMsgId(),
                DamageSource.LAVA.getMsgId(),
                DamageSource.HOT_FLOOR.getMsgId()
        );





        // Config build
        BUILDER.push("config");

        // Define configs

        // Minerals configs
        BUILDER.push("world");
        BUILDER.push("minerals");

        WOLFRAMITE_SMALL_VEINS_PER_CHUNK = BUILDER
                .worldRestart()
                .comment(" Small wolframite ore veins that generate per chunk.")
                .comment(" Generate up to n veins per chunk. [default: 4]")
                .define("wolframite_small_vein_count", 4);
        WOLFRAMITE_LARGE_VEINS_PER_CHUNK = BUILDER
                .worldRestart()
                .comment(" Large wolframite ore veins that generate per chunk.")
                .comment(" Generate once for every n chunks. [default: 2]")
                .define("wolframite_large_vein_count", 2);

        WOLFRAMITE_VEINS_SIZE_SMALL = BUILDER
                .worldRestart()
                .comment(" Max size of the small wolframite ore veins. [default: 3]")
                .define("wolframite_small_vein_size", 3);
        WOLFRAMITE_VEINS_SIZE_LARGE = BUILDER
                .worldRestart()
                .comment(" Max size of the large wolframite ore veins. [default: 7]")
                .define("wolframite_large_vein_size", 7);

        BUILDER.pop();

        // Mob configs
        BUILDER.push("mobs");
        BUILDER.push("infected");
        BUILDER.push("fungi");



        DIFFICULTY_MULTIPLIER = BUILDER
                .comment(" Affects the spawn count of the mod enemies, their attribute values, drops amounts, etc. [default: 1.0f]")
                .define("spawn_count_multiplier", 1.0f);

        GLOBAL_HEALTH_MULTIPLIER = BUILDER
                .comment(" Health multiplier for all the mod mobs. [default: 1.0f]")
                .define("health_multiplier", 1.0f);

        GLOBAL_ARMOR_MULTIPLIER = BUILDER
                .comment(" Armor multiplier for all the mod mobs. [default: 1.0f]")
                .define("armor_multiplier", 1.0f);
        GLOBAL_DAMAGE_MULTIPLIER = BUILDER
                .comment(" Damage multiplier for all the mod mobs. [default: 1.0f]")
                .define("damage_multiplier", 1.0f);

        MOBS_IGNORE_SUN = BUILDER
                .comment(" Mobs can spawn under sun light. [default: false]")
                .define("sun_light_spawn", false);
        MOBS_SUN_BURN = BUILDER
                .comment(" Mobs can be set on fire under the sun. [default: false]")
                .define("sun_light_burn", false);



        ForgeConfigSpec.Builder Temp = BUILDER
                .comment(" Damage sources that deal extra damage to the mobs.")
                .comment(" Damage sources in Vanilla: ");

        for (String name : damageSourceNames) {
            Temp.comment(" " + name);
        }

        Temp.comment(" Default:[");
        for (String name : defaultDamageSources) Temp.comment(" " + name);
        Temp.comment(" ]");

        MOBS_DAMAGE_SOURCE_WEAKNESSES = Temp.defineList(
                "damage_source_weaknesses",
                defaultDamageSources,
                object -> object instanceof String dmgName && damageSourceNames.contains(dmgName)
        );



        MOBS_MATERIALS_WEAKNESSES = BUILDER
                .comment("")
                .defineList(
                  "materials_weaknesses",
                  List.of(ModItems.WOLFRAMIUM_INGOT.get().toString(),
                          Items.BLAZE_ROD.toString(),
                          Items.BLAZE_POWDER.toString(),
                          Items.MAGMA_CREAM.toString(),
                          Items.MAGMA_BLOCK.toString(),
                          Items.LAVA_BUCKET.toString(),
                          Items.FIRE_CHARGE.toString()
                  ),
                  object -> true
                );

        MOBS_WEAKNESS_MULTIPLIER = BUILDER
                .comment(" Extra damage multiplier.")
                .define("weakness_damage_multiplier", 4.0f);

        BUILDER.pop(3);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

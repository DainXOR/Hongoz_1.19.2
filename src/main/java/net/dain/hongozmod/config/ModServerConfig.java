package net.dain.hongozmod.config;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ModServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;


    static {
        BUILDER.push("hongoz_server_config");

        // Define configs

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

package net.dain.hongozmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("hongoz_client_config");

        // Define configs

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

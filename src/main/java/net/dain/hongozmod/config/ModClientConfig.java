package net.dain.hongozmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Hongoz Configuration");

        // Define configs

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

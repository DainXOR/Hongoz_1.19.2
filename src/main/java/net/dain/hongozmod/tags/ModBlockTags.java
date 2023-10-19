package net.dain.hongozmod.tags;

import net.dain.hongozmod.HongozMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ModBlockTags {
    public static final TagKey<Block> NEEDS_WOLFRAMIUM_TOOL = create("needs_wolframium_tool");
    public static final TagKey<Block> MINEABLE_WITH_SWORD = create("mineable/sword");


    private ModBlockTags(){}

    private static @NotNull TagKey<Block> create(String name) {
        return ModBlockTags.create(new ResourceLocation(HongozMod.MOD_ID, name));
    }
    public static @NotNull TagKey<Block> create(ResourceLocation name) {
        return BlockTags.create(name);
    }
}

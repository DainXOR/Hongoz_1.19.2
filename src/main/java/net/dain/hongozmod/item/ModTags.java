package net.dain.hongozmod.item;

import net.dain.hongozmod.HongozMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_WOLFRAMIUM_TOOL =
                tag("needs_wolframium_tool");


        public static TagKey<Block> tag(String name){
            return BlockTags.create(new ResourceLocation(HongozMod.MOD_ID, name));
        }

        public static TagKey<Block> forgeTag(String name){
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }
}

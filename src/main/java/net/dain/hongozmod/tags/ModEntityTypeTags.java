package net.dain.hongozmod.tags;

import net.dain.hongozmod.HongozMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> CATDYCEPS_INFECTED = create("infected/catdyceps");

    private ModEntityTypeTags() {
    }

    private static @NotNull TagKey<EntityType<?>> create(String name) {
        return create(new ResourceLocation(HongozMod.MOD_ID, name));
    }
    private static @NotNull TagKey<EntityType<?>> create(ResourceLocation resource) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, resource);
    }
}

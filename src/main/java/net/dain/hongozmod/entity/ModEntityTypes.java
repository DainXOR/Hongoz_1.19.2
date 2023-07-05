package net.dain.hongozmod.entity;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HongozMod.MOD_ID);

    public static final RegistryObject<EntityType<ZhongoEntity>> ZHONGO =
            ENTITY_TYPES.register("zhongo", () -> EntityType.Builder.of(ZhongoEntity::new, MobCategory.MONSTER)
                    .sized(0.4f, 2.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "zhongo").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);

    }
}

package net.dain.hongozmod.entity;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.CroaktarEntity;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.dain.hongozmod.entity.custom.HordenEntity;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
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

    public static final RegistryObject<EntityType<HordenEntity>> HORDEN =
            ENTITY_TYPES.register("horden", () -> EntityType.Builder.of(HordenEntity::new, MobCategory.MONSTER)
                    .sized(2.0f, 2.4f)
                    .fireImmune()
                    .immuneTo(Blocks.LAVA)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "horden").toString()));

    public static final RegistryObject<EntityType<CroaktarEntity>> CROAKTAR =
            ENTITY_TYPES.register("croaktar", () -> EntityType.Builder.of(CroaktarEntity::new, MobCategory.MONSTER)
                    .sized(2.0f, 2.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "croaktar").toString()));

    public static final RegistryObject<EntityType<HonziadeEntity>> HONZIADE =
            ENTITY_TYPES.register("honziade", () -> EntityType.Builder.of(HonziadeEntity::new, MobCategory.MONSTER)
                    .sized(3.6f, 2.2f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "honziade").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);

    }
}

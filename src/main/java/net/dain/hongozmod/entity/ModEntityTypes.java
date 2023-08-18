package net.dain.hongozmod.entity;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.*;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.dain.hongozmod.entity.custom.hunter.HunterTest;
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

    public static final RegistryObject<EntityType<FungiEgg>> FUNGI_EGG =
            ENTITY_TYPES.register("fungi_egg", () -> EntityType.Builder.of(FungiEgg::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.3f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "fungi_egg").toString()));

    public static final RegistryObject<EntityType<Maggot>> MAGGOT =
            ENTITY_TYPES.register("fungi_maggot", () -> EntityType.Builder.of(Maggot::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.3f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "fungi_maggot").toString()));

    public static final RegistryObject<EntityType<BeaconEntity>> BEACON =
            ENTITY_TYPES.register("beacon", () -> EntityType.Builder.of(BeaconEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "beacon").toString()));

    public static final RegistryObject<EntityType<ZhongoEntity>> ZHONGO =
            ENTITY_TYPES.register("zhongo", () -> EntityType.Builder.of(ZhongoEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 2.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "zhongo").toString()));

    public static final RegistryObject<EntityType<HordenEntity>> HORDEN =
            ENTITY_TYPES.register("horden", () -> EntityType.Builder.of(HordenEntity::new, MobCategory.MONSTER)
                    .sized(1.4f, 2.6f)
                    .canSpawnFarFromPlayer()
                    .fireImmune()
                    .immuneTo(Blocks.LAVA)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "horden").toString()));

    public static final RegistryObject<EntityType<CroaktarEntity>> CROAKTAR =
            ENTITY_TYPES.register("croaktar", () -> EntityType.Builder.of(CroaktarEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "croaktar").toString()));

    public static final RegistryObject<EntityType<HonziadeEntity>> HONZIADE =
            ENTITY_TYPES.register("honziade", () -> EntityType.Builder.of(HonziadeEntity::new, MobCategory.MONSTER)
                    .sized(1.4f, 1.1f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "honziade").toString()));

    public static final RegistryObject<EntityType<HunterEntity>> HUNTER =
            ENTITY_TYPES.register("hunter", () -> EntityType.Builder.of(HunterEntity::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "hunter").toString()));

    public static final RegistryObject<EntityType<HunterTest>> HUNTER_TEST =
            ENTITY_TYPES.register("hunter_test", () -> EntityType.Builder.of(HunterTest::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.0f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "hunter").toString()));

    public static final RegistryObject<EntityType<EvoCroaktar>> EVO_CROAKTAR =
            ENTITY_TYPES.register("evolved_croaktar", () -> EntityType.Builder.of(EvoCroaktar::new, MobCategory.MONSTER)
                    .sized(2.6f, 2.8f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "evolved_croaktar").toString()));

    public static final RegistryObject<EntityType<HonziadeEntity.Queen>> HONZIADE_QUEEN =
            ENTITY_TYPES.register("honziade_queen", () -> EntityType.Builder.of(HonziadeEntity.Queen::new, MobCategory.MONSTER)
                    .sized(3.9f, 2.5f)
                    .build(new ResourceLocation(HongozMod.MOD_ID, "honziade").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);

    }
}

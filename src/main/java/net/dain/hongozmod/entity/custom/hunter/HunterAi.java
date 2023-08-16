package net.dain.hongozmod.entity.custom.hunter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.dain.hongozmod.ai.ModSensorType;
import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.HunterEntity.*;
import net.minecraft.world.entity.ai.behavior.warden.SetRoarTarget;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.HunterEntity.HunterEntity;
import net.minecraft.world.entity.monster.HunterEntity.HunterEntityAi;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public class HunterAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
    private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
    private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
    private static final int MELEE_ATTACK_COOLDOWN = 18;
    public static final int ROAR_DURATION = Mth.ceil(84.0F);
    private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
    private static final List<SensorType<? extends Sensor<? super HunterEntity>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, ModSensorType.HUNTER_ENTITY_SENSOR);

    // See
    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.ROAR_TARGET, MemoryModuleType.DISTURBANCE_LOCATION, MemoryModuleType.RECENT_PROJECTILE, MemoryModuleType.IS_SNIFFING, MemoryModuleType.ROAR_SOUND_DELAY, MemoryModuleType.ROAR_SOUND_COOLDOWN, MemoryModuleType.TOUCH_COOLDOWN, MemoryModuleType.VIBRATION_COOLDOWN, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_DELAY);


    public static void updateActivity(HunterEntity entity) {
        entity.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.IDLE));
    }

    protected static Brain<?> makeBrain(HunterEntity entity, Dynamic<?> dynamic) {
        Brain.Provider<HunterEntity> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
        Brain<HunterEntity> brain = provider.makeBrain(dynamic);
        initCoreActivity(brain);
        initIdleActivity(brain);
        initRoarActivity(brain);
        initFightActivity(entity, brain);
        initInvestigateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<HunterEntity> pBrain) {
        pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new SetHunterLookTarget(), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<HunterEntity> pBrain) {
        pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(new SetRoarTarget<>(HunterEntity::getEntityAngryAt), new TryToSniff(), new RunOne<>(ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(new RandomStroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1)))));
    }

    private static void initInvestigateActivity(Brain<HunterEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.INVESTIGATE, 5, ImmutableList.of(new SetRoarTarget<>(HunterEntity::getEntityAngryAt), new GoToTargetLocation<>(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)), MemoryModuleType.DISTURBANCE_LOCATION);
    }

    private static void initSniffingActivity(Brain<HunterEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.SNIFF, 5, ImmutableList.of(new SetRoarTarget<>(HunterEntity::getEntityAngryAt), new Sniffing<>(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING);
    }

    private static void initRoarActivity(Brain<HunterEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
    }

    private static void initFightActivity(HunterEntity pHunterEntity, Brain<HunterEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(DIG_COOLDOWN_SETTER, new StopAttackingIfTargetInvalid<>((p_219540_) -> {
            return !pHunterEntity.getAngerLevel().isAngry() || !pHunterEntity.canTargetEntity(p_219540_);
        }, HunterEntityAi::onTargetInvalid, false), new SetEntityLookTarget((p_219535_) -> {
            return isTarget(pHunterEntity, p_219535_);
        }, (float)pHunterEntity.getAttributeValue(Attributes.FOLLOW_RANGE)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F), new SonicBoom(), new MeleeAttack(18)), MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isTarget(HunterEntity pHunterEntity, LivingEntity pEntity) {
        return pHunterEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter((p_219509_) -> {
            return p_219509_ == pEntity;
        }).isPresent();
    }

    private static void onTargetInvalid(HunterEntity p_219529_, LivingEntity p_219530_) {
        if (!p_219529_.canTargetEntity(p_219530_)) {
            p_219529_.clearAnger(p_219530_);
        }

        setDigCooldown(p_219529_);
    }

    public static void setDigCooldown(LivingEntity pEntity) {
        if (pEntity.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
            pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }

    }

    public static void setDisturbanceLocation(HunterEntity pHunterEntity, BlockPos pDisturbanceLocation) {
        if (pHunterEntity.level.getWorldBorder().isWithinBounds(pDisturbanceLocation) && !pHunterEntity.getEntityAngryAt().isPresent() && !pHunterEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            setDigCooldown(pHunterEntity);
            pHunterEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
            pHunterEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pDisturbanceLocation), 100L);
            pHunterEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, pDisturbanceLocation, 100L);
            pHunterEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
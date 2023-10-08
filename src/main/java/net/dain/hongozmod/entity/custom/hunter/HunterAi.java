package net.dain.hongozmod.entity.custom.hunter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.warden.Digging;
import net.minecraft.world.entity.ai.behavior.warden.Emerging;
import net.minecraft.world.entity.ai.behavior.warden.ForceUnmount;
import net.minecraft.world.entity.ai.behavior.warden.Roar;
import net.minecraft.world.entity.ai.behavior.warden.SetRoarTarget;
import net.minecraft.world.entity.ai.behavior.warden.SetWardenLookTarget;
import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class HunterAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
    private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
    private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
    private static final int MELEE_ATTACK_COOLDOWN = 18;
    public static final int ROAR_DURATION = Mth.ceil(84.0F);
    public static final int DIGGING_COOLDOWN = 1200;
    private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
    private static final List<SensorType<? extends Sensor<? super HunterTest>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.ROAR_TARGET, MemoryModuleType.DISTURBANCE_LOCATION, MemoryModuleType.RECENT_PROJECTILE, MemoryModuleType.IS_SNIFFING, MemoryModuleType.IS_EMERGING, MemoryModuleType.ROAR_SOUND_DELAY, MemoryModuleType.DIG_COOLDOWN, MemoryModuleType.ROAR_SOUND_COOLDOWN, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleType.TOUCH_COOLDOWN, MemoryModuleType.VIBRATION_COOLDOWN, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_DELAY);

    public static void updateActivity(HunterTest pHunter) {
        pHunter.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.IDLE));
    }

    protected static Brain<?> makeBrain(HunterTest pHunter, Dynamic<?> dynamic) {
        Brain.Provider<HunterTest> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
        Brain<HunterTest> brain = provider.makeBrain(dynamic);
        initCoreActivity(brain);
        initIdleActivity(brain);
        initRoarActivity(brain);
        initFightActivity(pHunter, brain);
        initInvestigateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<HunterTest> pBrain) {
        pBrain.addActivity(
                Activity.CORE, 0,
                ImmutableList.of(
                        new Swim(0.8F),
                        new SetWardenLookTarget(),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink()));
    }
    private static void initIdleActivity(Brain<HunterTest> pBrain) {
        pBrain.addActivity(
                Activity.IDLE, 10,
                ImmutableList.of(
                        new SetRoarTarget<>(HunterTest::getEntityAngryAt),
                        new RunOne<>(
                                ImmutableMap.of(),
                                ImmutableList.of(
                                        Pair.of(new RandomStroll(0.5F), 2),
                                        Pair.of(new DoNothing(30, 60), 1))
                        )
                )
        );
    }

    private static void initInvestigateActivity(Brain<HunterTest> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.INVESTIGATE, 5, ImmutableList.of(new SetRoarTarget<>(HunterTest::getEntityAngryAt), new GoToTargetLocation<>(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)), MemoryModuleType.DISTURBANCE_LOCATION);
    }
    private static void initRoarActivity(Brain<HunterTest> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
    }

    private static void initFightActivity(HunterTest pWarden, Brain<HunterTest> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new StopAttackingIfTargetInvalid<>((p_219540_) -> {
            return !pWarden.getAngerLevel().isAngry() || !pWarden.canTargetEntity(p_219540_);
        }, HunterAi::onTargetInvalid, false), new SetEntityLookTarget((p_219535_) -> {
            return isTarget(pWarden, p_219535_);
        }, (float)pWarden.getAttributeValue(Attributes.FOLLOW_RANGE)), new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.2F), new SonicBoom(), new MeleeAttack(18)), MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isTarget(HunterTest pWarden, LivingEntity pEntity) {
        return pWarden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter((entity) -> entity == pEntity).isPresent();
    }

    private static void onTargetInvalid(HunterTest hunter, LivingEntity entity) {
        if (!hunter.canTargetEntity(entity)) {
            hunter.clearAnger(entity);
        }
    }

    public static void setDisturbanceLocation(HunterTest pWarden, BlockPos pDisturbanceLocation) {
        if (pWarden.level.getWorldBorder().isWithinBounds(pDisturbanceLocation) && pWarden.getEntityAngryAt().isEmpty() && pWarden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
            pWarden.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
            pWarden.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pDisturbanceLocation), 100L);
            pWarden.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, pDisturbanceLocation, 100L);
            pWarden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
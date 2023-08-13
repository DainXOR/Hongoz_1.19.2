package net.dain.hongozmod.entity.custom.hunter;

import net.dain.hongozmod.entity.custom.HordenEntity;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Collections;

import static net.minecraft.world.entity.Pose.ROARING;

public class HunterEntity extends Infected implements VibrationListener.VibrationListenerConfig {
    private static final int GAME_EVENT_LISTENER_RANGE = 32;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int MAX_HEALTH = 750;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
    private static final float KNOCKBACK_RESISTANCE = 0.8F;
    private static final float ATTACK_KNOCKBACK = 2.0F;
    private static final int ATTACK_DAMAGE = 15;

    private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
    private static final int ANGERMANAGEMENT_TICK_DELAY = 20;
    private static final int DEFAULT_ANGER = 35;
    private static final int PROJECTILE_ANGER = 10;
    private static final int ON_HURT_ANGER_BOOST = 20;
    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int PROJECTILE_ANGER_DISTANCE = 30;


    public static final int HORDEN_SPAWN_REQUIREMENT = 10;
    public static int SPAWN_COUNT = 0;
    public static int ALIVE_COUNT = 0;

    public net.minecraft.world.entity.AnimationState roarAnimationState = new net.minecraft.world.entity.AnimationState();
    public net.minecraft.world.entity.AnimationState attackAnimationState = new net.minecraft.world.entity.AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
    //private AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());

    public HunterEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), GAME_EVENT_LISTENER_RANGE, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
        this.xpReward = 150;

        // Como dijo el ciego
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 12.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 12.0F);
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED, 2.00)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .build();
    }

    @Override
    public void onAddedToWorld() {
        SPAWN_COUNT += 1;
        ALIVE_COUNT += 1;
        super.onAddedToWorld();
    }
    @Override
    public void onRemovedFromWorld() {
        ALIVE_COUNT -= 1;
        super.onRemovedFromWorld();
    }

    @Override
    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return 0.0f;
    }

    @Override
    protected boolean canRide(Entity pVehicle) {
        return false;
    }

    @Override
    public boolean canDisableShield() {
        return true;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.55F;
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CLIENT_ANGER_LEVEL, 0);
    }

    public int getClientAngerLevel() {
        return this.entityData.get(CLIENT_ANGER_LEVEL);
    }
    //private void syncClientAngerLevel() {
    //    this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
    //}

    //protected void customServerAiStep() {
    //    ServerLevel serverlevel = (ServerLevel)this.level;
    //    serverlevel.getProfiler().push("hunterBrain");
    //    this.getBrain().tick(serverlevel, this);
    //    this.level.getProfiler().pop();
    //    super.customServerAiStep();
//
    //    if (this.tickCount % 20 == 0) {
    //        this.angerManagement.tick(serverlevel, this::canTargetEntity);
    //        this.syncClientAngerLevel();
    //    }
//
    //    WardenAi.updateActivity(this);
    //}

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_POSE.equals(pKey) && this.getPose() == ROARING) {
            this.roarAnimationState.start(this.tickCount);
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.WARDEN_CAN_LISTEN;
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    /**
     * Warden doesn't have goals
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.6f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.8d, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractGolem.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, false));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Animal.class, false));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));

    }

    @Override
    public Class<? extends Infected> getAngryAlertType() {
        return HunterEntity.class;
    }

    @Override
    protected <E extends IAnimatable> PlayState specialAnimation(AnimationEvent<E> event) {
        this.getAnimation("roar", LoopType.PLAY_ONCE);
        this.getAnimation("sniff", LoopType.PLAY_ONCE);
        this.getAnimation("jump", LoopType.PLAY_ONCE);
        this.getAnimation("smash", LoopType.PLAY_ONCE);
        return super.specialAnimation(event);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasPose(ROARING)? ModSounds.HUNTER_AGGRESIVE.get() : ModSounds.HUNTER_AMBIENT.get();
    }
    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(ModSounds.HUNTER_STEP.get());
        super.playStepSound(blockPos, blockState);
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.HUNTER_HURT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HUNTER_DEATH.get();
    }

    @Override
    public boolean shouldListen(ServerLevel pLevel, GameEventListener pListener, BlockPos pPos, GameEvent pGameEvent, GameEvent.Context pContext) {
        return false;
    }

    @Override
    public void onSignalReceive(ServerLevel pLevel, GameEventListener pListener, BlockPos pSourcePos, GameEvent pGameEvent, @Nullable Entity pSourceEntity, @Nullable Entity pProjectileOwner, float pDistance) {

    }

    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        boolean minHordenSpawnsReached = HordenEntity.SPAWN_COUNT > 0 && HordenEntity.SPAWN_COUNT % HORDEN_SPAWN_REQUIREMENT == 0;
        boolean spawnReasons = pSpawnReason.equals(MobSpawnType.NATURAL) || pSpawnReason.equals(MobSpawnType.MOB_SUMMONED);
        boolean isCappedSpawn = spawnReasons && minHordenSpawnsReached;
        boolean isNonCappedSpawn = !spawnReasons;

        return (isCappedSpawn || isNonCappedSpawn) && super.checkSpawnRules(pLevel, pSpawnReason);
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity pTarget) {
        return !(pTarget instanceof HunterEntity) && super.canAttack(pTarget);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return pSource.getEntity() instanceof HunterEntity || super.isInvulnerableTo(pSource);
    }

    public static boolean checkHunterSpawnRules(EntityType<HunterEntity> pHunter, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom){
        boolean heightCondition = (pPos.getY() < 60/*Sea level: 63*/) || isHalloween();
        return heightCondition && checkAnyLightMonsterSpawnRules(pHunter, pLevel, pSpawnType, pPos, pRandom);
    }

    private static boolean isHalloween() {
        LocalDate localdate = LocalDate.now();
        int i = localdate.get(ChronoField.DAY_OF_MONTH);
        int j = localdate.get(ChronoField.MONTH_OF_YEAR);
        return j == 10 && i >= 20 || j == 11 && i <= 3;
    }
}

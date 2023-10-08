package net.dain.hongozmod.entity.custom.hunter;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.dain.hongozmod.entity.custom.HordenEntity;
import net.dain.hongozmod.entity.templates.AngerLevel;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
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
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.world.entity.Pose.ROARING;

public class HunterEntity extends Infected implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int GAME_EVENT_LISTENER_RANGE = 32;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int MAX_HEALTH = 750;
    private static final float FIGHTING_SPEED_MULTIPLIER = 0.3F;
    private static final float KNOCKBACK_RESISTANCE = 0.8F;
    private static final float ATTACK_KNOCKBACK = 2.0F;
    private static final int ATTACK_DAMAGE = 15;

    private static final int ANGER_UPDATE_DELAY = 20 * 2;
    private static final int LISTEN_ANGER = 5;
    private static final int MELEE_ANGER = 30;
    private static final int PROJECTILE_SOFT_ANGER = 10;
    private static final int PROJECTILE_HARD_ANGER = 20;

    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int PROJECTILE_ANGER_DISTANCE = 30;

    public static final int HORDEN_SPAWNS_REQUIREMENT = 10;
    public static int SPAWN_COUNT = 0;
    public static int ALIVE_COUNT = 0;

    private int angerDelayTimer = 0;
    private AngerLevel angerLevel = AngerLevel.BASE;
    private int angerAmount = angerLevel.getValue();

    private int tendrilAnimation;
    private int tendrilAnimationO;

    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState jumpAnimationState = new AnimationState();
    public AnimationState smashAnimationState = new AnimationState();
    public AnimationState chargeAnimationState = new AnimationState();

    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;

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

    public void goBerserk(){}

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
    public float getWalkTargetValue(@NotNull BlockPos pPos, @NotNull LevelReader pLevel) {
        return 0.0f;
    }

    @Override
    protected boolean canRide(@NotNull Entity pVehicle) {
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
        //this.entityData.define(CLIENT_ANGER_LEVEL, BASE_ANGER);
    }

    //@Contract("null->false")
    //public boolean canTargetEntity(@Nullable Entity entity) {
    //    return  entity instanceof LivingEntity livingentity &&
    //            this.level == entity.level &&
    //            EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) &&
    //            !this.isAlliedTo(entity) &&
    //            livingentity.getType() != EntityType.ARMOR_STAND &&
    //            livingentity.getType() != ModEntityTypes.HUNTER.get() &&
    //            !livingentity.isInvulnerable() &&
    //            !livingentity.isDeadOrDying() &&
    //            this.level.getWorldBorder().isWithinBounds(livingentity.getBoundingBox());
    //}

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("anger", this.getAngerValue());

        VibrationListener.codec(this)
                .encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener())
                .resultOrPartial(LOGGER::error)
                .ifPresent((tag) -> pCompound.put("listener", tag));
    }
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setAngerLevel(pCompound.getInt("anger"));

        if (pCompound.contains("listener", 10)) {
            VibrationListener.codec(this)
                    .parse(new Dynamic<>(NbtOps.INSTANCE, pCompound.getCompound("listener")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent((vibrationListener) -> this.dynamicGameEventListener.updateListener(vibrationListener, this.level));
        }

    }

    public AngerLevel getAngerLevel() {
        return this.angerLevel;
    }
    public int getAngerValue() {
        return this.angerAmount;
    }

    public Boolean setAngerLevel(AngerLevel level) {
        if(level.testCondition(this)){
            this.angerLevel = level;
            this.angerAmount = level.getValue();
            return true;
        }
        return false;
    }
    public void forceAngerLevel(AngerLevel level) {
        this.angerLevel = level;
        this.angerAmount = level.getValue();
    }

    public Boolean setAngerLevel(int angerAmount) {
        AngerLevel newAngerLevel = AngerLevel.getLevel(Math.max(angerAmount, AngerLevel.BASE.getValue()), this);
        if(newAngerLevel != this.angerLevel){
            this.angerLevel = AngerLevel.getLevel(Math.max(angerAmount, AngerLevel.BASE.getValue()), this);
            this.angerAmount = angerAmount;
            return true;
        }
        return false;
    }
    public void forceAngerLevel(int angerAmount) {
        this.angerLevel = AngerLevel.getLevelByValue(Math.max(angerAmount, AngerLevel.BASE.getValue()));
        this.angerAmount = angerAmount;
    }

    public void clearAnger() {
        this.setAngerLevel(AngerLevel.BASE);
    }
    public void increaseAnger(int angerAmount) {
        this.setAngerLevel(this.getAngerValue() + angerAmount);
    }
    public void reduceAnger(int angerAmount) {
        this.setAngerLevel(this.getAngerValue() - angerAmount);
    }

    @VisibleForTesting
    public void increaseAnger(int angerAmount, boolean pPlayListeningSound) {
        if (!this.isNoAi()) {
            this.increaseAnger(angerAmount);

            if (pPlayListeningSound) {
                this.playListeningSound();
            }
        }
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean flag = super.hurt(pSource, pAmount);

        if (this.level.isClientSide || this.isNoAi()) {
            return flag;
        }
        
        Entity entity = pSource.getEntity();
        if(pSource.isProjectile()){
            this.increaseAnger((int)pAmount + 10, false);

        } else if (pSource.isMagic() || pSource.isFire()) {
            this.increaseAnger(5, false);

            if(this.angerLevel.equalsOrAbove(AngerLevel.DESPERATE)){
                this.goBerserk();
            }
        }
        this.increaseAnger(20, false);
        if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty() && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!(pSource instanceof IndirectEntityDamageSource) || this.closerThan(livingEntity, 5.0D)) {
                this.setAttackTarget(livingEntity);
            }
        }


        return flag;
    }

    public void setAttackTarget(LivingEntity pAttackTarget) {
        this.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        StartAttacking.setAttackTarget(this, pAttackTarget);
        //SonicBoom.setCooldown(this, 200);
    }

    protected void doPush(Entity pEntity) {
        if (!this.isNoAi() && !this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
            this.increaseAnger(pEntity);
            // WardenAi.setDisturbanceLocation(this, pEntity.blockPosition());
        }
        super.doPush(pEntity);
    }

    public boolean shouldListen(ServerLevel pLevel, GameEventListener pListener, BlockPos pPos, GameEvent pGameEvent, GameEvent.Context pContext) {
        if (!this.isNoAi() && !this.isDeadOrDying() && !this.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN) && pLevel.getWorldBorder().isWithinBounds(pPos) && !this.isRemoved() && this.level == pLevel) {
            Entity entity = pContext.sourceEntity();
            if (entity instanceof LivingEntity livingEntity) {
                if (!this.canTargetEntity(livingEntity)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void onSignalReceive(ServerLevel pLevel, GameEventListener pListener, BlockPos pSourcePos, GameEvent pGameEvent, @Nullable Entity pSourceEntity, @Nullable Entity pProjectileOwner, float pDistance) {
        if (!this.isDeadOrDying()) {
            this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
            pLevel.broadcastEntityEvent(this, (byte)61);
            this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0F, this.getVoicePitch());
            BlockPos blockpos = pSourcePos;
            if (pProjectileOwner != null) {
                if (this.closerThan(pProjectileOwner, 30.0D)) {
                    if (this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
                        if (this.canTargetEntity(pProjectileOwner)) {
                            blockpos = pProjectileOwner.blockPosition();
                        }

                        this.increaseAnger(pProjectileOwner);
                    } else {
                        this.increaseAnger(pProjectileOwner, 10, true);
                    }
                }

                this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
            } else {
                this.increaseAnger(pSourceEntity);
            }

            if (!this.getAngerLevel().isAngry()) {
                Optional<LivingEntity> optional = this.angerManagement.getActiveEntity();
                if (pProjectileOwner != null || optional.isEmpty() || optional.get() == pSourceEntity) {
                    //HunterAi.setDisturbanceLocation(this, blockpos);
                }
            }

        }
    }

    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        return new GroundPathNavigation(this, pLevel) {
            protected @NotNull PathFinder createPathFinder(int pMaxVisitedNodes) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes) {
                    protected float distance(@NotNull Node pFirst, @NotNull Node pSecond) {
                        return pFirst.distanceToXZ(pSecond);
                    }
                };
            }
        };
    }

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
    public @NotNull TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.WARDEN_CAN_LISTEN;
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    /**
     * Warden doesn't have goals
     */
    /*
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
    */


    @Override
    public boolean canBeHurtBy(Entity pEntity) {
        return true;
    }

    @Override
    public boolean customAddEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
        return  !(pEffectInstance.getEffect().isBeneficial() &&
                (!(pEntity instanceof HunterEntity ||
                        (pEntity instanceof AreaEffectCloud aoeCloud &&
                                aoeCloud.getOwner() instanceof Infected))));
    }

    @Override
    public Class<? extends Infected> getAngryAlertType() {
        return HunterEntity.class;
    }

    @Override
    protected <E extends IAnimatable> PlayState specialAnimation(AnimationEvent<E> event) {
        event.getController().setAnimation(this.getAnimation("investigate", LoopType.PLAY_ONCE));
        this.getAnimation("roar", LoopType.PLAY_ONCE);
        this.getAnimation("smash", LoopType.PLAY_ONCE);
        this.getAnimation("jump_smash", LoopType.PLAY_ONCE);
        this.getAnimation("charge", LoopType.PLAY_ONCE);
        return super.specialAnimation(event);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch();
    }

    public SoundEvent getListeningSound(){
        return this.angerLevel.equalsOrAbove(AngerLevel.ANGRY)? SoundEvents.WARDEN_LISTENING_ANGRY : SoundEvents.WARDEN_LISTENING;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasPose(ROARING)? ModSounds.HUNTER_AGGRESIVE.get() : ModSounds.HUNTER_AMBIENT.get();
    }
    @Override
    protected void playStepSound(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        this.playSound(ModSounds.HUNTER_STEP.get());
        super.playStepSound(blockPos, blockState);
    }
    private void playListeningSound() {
        if (!this.hasPose(Pose.ROARING)) {
            this.playSound(this.getListeningSound(), 10.0F, this.getVoicePitch());
        }
    }
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.HUNTER_HURT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HUNTER_DEATH.get();
    }

    @Override
    public boolean checkSpawnRules(@NotNull LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        boolean minHordenSpawnsReached = HordenEntity.SPAWN_COUNT > 0 && HordenEntity.SPAWN_COUNT % HORDEN_SPAWNS_REQUIREMENT == 0;
        boolean spawnReasons = pSpawnReason.equals(MobSpawnType.NATURAL);
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
        int i = localdate.getDayOfMonth();
        int j = localdate.getMonth().getValue();
        return j == 10 && i >= 20 || j == 11 && i <= 3;
    }

}

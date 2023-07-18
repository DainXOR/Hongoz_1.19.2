package net.dain.hongozmod.entity.custom;

import com.mojang.logging.LogUtils;
import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.UUID;

import static net.minecraft.world.entity.Pose.ROARING;

public class HunterEntity extends Monster implements IAnimatable, NeutralMob, VibrationListener.VibrationListenerConfig {
    public static final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("animation.hunter.idle", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("animation.hunter.walk", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder ATTACK_ANIMATION = new AnimationBuilder().addAnimation("animation.hunter.roar", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationBuilder ROAR_ANIMATION = new AnimationBuilder().addAnimation("animation.hunter.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final int ALERT_RANGE_Y = 32;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private UUID persistentAngerTarget;
    private int remainingPersistentAngerTime;
    private int ticksUntilNextAlert;

    private static final int GAME_EVENT_LISTENER_RANGE = 16;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int MAX_HEALTH = 500;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
    private static final float KNOCKBACK_RESISTANCE = 1.0F;
    private static final float ATTACK_KNOCKBACK = 1.5F;
    private static final int ATTACK_DAMAGE = 30;
    public static final int HORDEN_SPAWN_REQUIREMENT = 20;
    public static int SPAWN_COUNT = 0;

    public net.minecraft.world.entity.AnimationState roarAnimationState = new net.minecraft.world.entity.AnimationState();
    public net.minecraft.world.entity.AnimationState attackAnimationState = new net.minecraft.world.entity.AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private boolean threaten = false;

    public HunterEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
        this.xpReward = 150;

        // Como dijo el ciego
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
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
    public void onAddedToWorld() {
        SPAWN_COUNT += 1;
        super.onAddedToWorld();
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 750.00)
                .add(Attributes.ATTACK_DAMAGE, 10.00)
                .add(Attributes.ATTACK_SPEED, 1.00)
                .add(Attributes.ATTACK_KNOCKBACK, 2.00)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 8)
                .build();
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

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
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.8d, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        //this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.targetSelector.addGoal(7, new ResetUniversalAngerTargetGoal<>(this, true));

    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel)this.level, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        super.customServerAiStep();
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        if(this.threaten){
            event.getController().markNeedsReload();
            event.getController().setAnimation(
                    new AnimationBuilder()
                            .addAnimation("animation.hunter.roar", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.setPose(ROARING);
            this.threaten = false;
            return PlayState.CONTINUE;
        }
        if(event.isMoving()){
            event.getController().setAnimation(
                    new AnimationBuilder()
                            .addAnimation("animation.hunter.walk", ILoopType.EDefaultLoopTypes.LOOP));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(
                new AnimationBuilder()
                        .addAnimation("animation.hunter.idle", ILoopType.EDefaultLoopTypes.LOOP));


        return PlayState.CONTINUE;

    }
    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if(this.swinging && event.getController().getAnimationState().equals(AnimationState.Stopped)){
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("animation.hunter.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, event -> {
            if(!this.swinging){
                if(event.isMoving()){
                    event.getController().setAnimation(WALK_ANIMATION);
                }
                else{
                    event.getController().setAnimation(IDLE_ANIMATION);
                }
                return PlayState.CONTINUE;
            }

            return PlayState.STOP;
        }));
        data.addAnimationController(new AnimationController(this, "attackController", 0, event -> {
            if(this.swinging){
                event.getController().setAnimation(ATTACK_ANIMATION);
                return PlayState.CONTINUE;
            }
            event.getController().markNeedsReload();
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }
    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.remainingPersistentAngerTime = pTime;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }
    @Override
    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertOthers();
            }

            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }
    private void alertOthers() {
        double d0 = ALERT_RANGE_Y; // this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        this.level.getEntitiesOfClass(HunterEntity.class, aabb, EntitySelector.NO_SPECTATORS).stream()
                .filter((entity) -> { return entity != this; })
                .filter((entity) -> { return entity.getTarget() == null; })
                .filter((entity) -> { return !entity.isAlliedTo(this.getTarget()); })
                .forEach((entity) -> entity.setTarget(this.getTarget()));
    }

    @Override
    public void setTarget(@Nullable LivingEntity pTarget) {
        if (this.getTarget() == null && pTarget != null) {
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
        if (pTarget instanceof Player) {
            this.setLastHurtByPlayer((Player)pTarget);
        }

        super.setTarget(pTarget);
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
    public boolean canAttack(LivingEntity pTarget) {
        return !(pTarget instanceof HunterEntity) && super.canAttack(pTarget);
    }
}

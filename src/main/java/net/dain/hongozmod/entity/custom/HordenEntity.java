package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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

public class HordenEntity extends Monster implements IAnimatable {
    public static final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("animation.horden.idle", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("animation.horden.walk", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder ATTACK_ANIMATION = new AnimationBuilder().addAnimation("animation.horden.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE);


    public static final int ALIVE_LIMIT = 10;
    public static int SPAWN_COUNT = 0;
    public static int ALIVE_COUNT = 0;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public HordenEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 35;
    }

    @Override
    public void onAddedToWorld() {
        ALIVE_COUNT += 1;
        SPAWN_COUNT += 1;
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        ALIVE_COUNT -= 1;
        super.onRemovedFromWorld();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setPersistenceRequired();

        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(pLevel.getLevel());
        assert lightningbolt != null;
        lightningbolt.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
        pLevel.addFreshEntity(lightningbolt);
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 5.0f, 1.0f);

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.00)
                .add(Attributes.ATTACK_DAMAGE, 8.00)
                .add(Attributes.ATTACK_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .add(Attributes.ATTACK_KNOCKBACK, 0.50)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.00)
                .add(Attributes.ARMOR, 0.90)
                .add(Attributes.ARMOR_TOUGHNESS, 0.40)
                .build();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float pAmount) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            return super.hurt(damageSource, 1);
        }
        return super.hurt(damageSource, pAmount);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2d, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 32));


        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
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
    }
    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if(this.swinging){
            event.getController().setAnimation(ATTACK_ANIMATION);
            return PlayState.CONTINUE;
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
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
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(ModSounds.HORDEN_STEP.get());
        super.playStepSound(blockPos, blockState);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HORDEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.HORDEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HORDEN_DEATH.get();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        boolean canSpawn = ALIVE_COUNT < ALIVE_LIMIT && super.checkSpawnRules(pLevel, pSpawnReason);

        return canSpawn;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }
}

package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
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

public class HonziadeEntity extends Spider implements IAnimatable{
    public static final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("animation.honziade.idle", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("animation.honziade.walk", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder ATTACK_ANIMATION = new AnimationBuilder().addAnimation("animation.honziade.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private final HonziadePart body;
    private final HonziadePart tail;
    private final HonziadePart[] subEntities;

    public HonziadeEntity(EntityType<? extends Spider> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 25;

        this.body = new HonziadePart(this, "body", 2.0f, 2.0f);
        this.tail = new HonziadePart(this, "tail", 1.5f, 1.0f);
        this.subEntities = new HonziadePart[]{body, tail};
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.00)
                .add(Attributes.ATTACK_DAMAGE, 1.50)
                .add(Attributes.ATTACK_SPEED, 0.02)
                .add(Attributes.MOVEMENT_SPEED, 0.60)
                .add(Attributes.FOLLOW_RANGE, 64.00)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.50)
                .build();
    }

    public static AttributeSupplier setQueenAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.00)
                .add(Attributes.ATTACK_DAMAGE, 2.00)
                .add(Attributes.ATTACK_SPEED, 0.10)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 128.00)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.50)
                .build();
    }

    /**
     *Called when the entity is attacked
     */
    @Override
    public boolean hurt(DamageSource damageSource, float pAmount) {
        if(this.level.isClientSide){
            return false;
        }

        int fireMultiplier = damageSource.isFire()? 3 : 1;
        return this.hurt(this.body, damageSource, pAmount * fireMultiplier);
    }

    public boolean hurt(HonziadePart pPart, DamageSource pSource, float pDamage) {
        if (pPart == this.tail){
            pDamage *= 4.0f;
        }
        return super.hurt(pSource, pDamage);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity pPassenger) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3d, false));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.2f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        //this.targetSelector.addGoal(2, new AvoidEntityGoal<>(this, Warden.class, 32.0f, 1.3f, 1.0f));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, false));
        //this.targetSelector.addGoal(3, new AvoidEntityGoal<>(this, IronGolem.class, 32.0f, 1.3f, 1.0f));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, false));
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.25F;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        if(event.isMoving()){
            event.getController().setAnimation(WALK_ANIMATION);
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(
                new AnimationBuilder()
                        .addAnimation("animation.honziade.idle", ILoopType.EDefaultLoopTypes.LOOP));


        return PlayState.CONTINUE;

    }
    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if(this.swinging && event.getController().getAnimationState().equals(AnimationState.Stopped)){
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("animation.honziade.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.swinging = false;
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
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HONZIADE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        this.playSound(SoundEvents.GHAST_HURT, 0.4F, 7.0F);
        this.playSound(SoundEvents.CREEPER_HURT, 0.5F, .75F);
        this.playSound(SoundEvents.SPIDER_HURT, 1.0F, 2.0F);

        return SoundEvents.HOSTILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        this.playSound(SoundEvents.GHAST_DEATH, 0.4F, 7.0F);
        this.playSound(SoundEvents.CREEPER_DEATH, 0.5F, 0.75F);
        this.playSound(SoundEvents.SPIDER_DEATH, 1.0F, 2.0F);

        return SoundEvents.HOSTILE_DEATH;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    public HonziadePart[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        // if (true) return; // Forge: Fix MC-158205: Moved into setId() (Ender dragon don't register melee hits properly)
        HonziadePart[] honziadeParts = this.getSubEntities();

        for(int i = 0; i < honziadeParts.length; ++i) {
            honziadeParts[i].setId(i + pPacket.getId());
        }

    }
}

package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
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
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    public HordenEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.00)
                .add(Attributes.ATTACK_DAMAGE, 5.00)
                .add(Attributes.ATTACK_SPEED, 1.00)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .add(Attributes.ATTACK_KNOCKBACK, 1.00)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.00)
                .add(Attributes.ARMOR, 0.80)
                .add(Attributes.ARMOR_TOUGHNESS, 0.20)
                .build();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float pAmount) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            return false;
        }
        return super.hurt(damageSource, pAmount);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2d, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        if(event.isMoving()){
            event.getController().setAnimation(
                    new AnimationBuilder()
                            .addAnimation("animation.horden.walk", ILoopType.EDefaultLoopTypes.LOOP));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(
                new AnimationBuilder()
                        .addAnimation("animation.horden.idle", ILoopType.EDefaultLoopTypes.LOOP));


        return PlayState.CONTINUE;

    }
    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if(this.swinging && event.getController().getAnimationState().equals(AnimationState.Stopped)){
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("animation.horden.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
        data.addAnimationController(new AnimationController(this, "attackController", 0, this::attackPredicate));
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
}

package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.w3c.dom.Attr;
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

public class CroaktarEntity extends Monster implements IAnimatable{
    public static final AnimationBuilder IDLE_ANIMATION = new AnimationBuilder().addAnimation("animation.croaktar.idle", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder WALK_ANIMATION = new AnimationBuilder().addAnimation("animation.croaktar.walk", ILoopType.EDefaultLoopTypes.LOOP);
    public static final AnimationBuilder ATTACK_ANIMATION = new AnimationBuilder().addAnimation("animation.croaktar.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public CroaktarEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 10;
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.00)
                .add(Attributes.ATTACK_DAMAGE, 8.00)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .add(Attributes.ATTACK_KNOCKBACK, 0.20)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3d, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {




        if(event.isMoving()){
            event.getController().setAnimation(IDLE_ANIMATION);
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(WALK_ANIMATION);


        return PlayState.CONTINUE;

    }
    private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {
        if(this.swinging && event.getController().getAnimationState().equals(AnimationState.Stopped)){
            event.getController().markNeedsReload();
            event.getController().setAnimation(ATTACK_ANIMATION);
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
        super.playStepSound(blockPos, blockState);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ZHONGO_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ZHONGO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ZHONGO_DEATH.get();
    }
}

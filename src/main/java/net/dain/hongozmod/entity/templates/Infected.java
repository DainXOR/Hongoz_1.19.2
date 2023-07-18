package net.dain.hongozmod.entity.templates;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
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

@ApiStatus.Experimental
public class Infected extends Monster implements IAnimatable, NeutralMob {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public static final AnimationBuilder IDLE_ANIMATION = newAnimation("idle", LoopType.LOOP);
    public static final AnimationBuilder WALK_ANIMATION = newAnimation("walk", LoopType.LOOP);
    public static final AnimationBuilder ATTACK_ANIMATION = newAnimation("attack", LoopType.PLAY_ONCE);

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;

    private static final String ENTITY_NAME = "infected";


    protected Infected(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.8d, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        // this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        // this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        // this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));

    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }
    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
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


    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }


    private <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        if(this.swinging){
            return PlayState.STOP;
        }

        if(event.isMoving()){
            event.getController().setAnimation(WALK_ANIMATION);
        }
        else{
            event.getController().setAnimation(IDLE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }
    private <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        if(this.swinging){
            event.getController().setAnimation(ATTACK_ANIMATION);
            return PlayState.CONTINUE;
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
    }
    private <E extends IAnimatable> PlayState specialAnimation(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(newController("controller", this::normalAnimation));
        data.addAnimationController(newController("attackController", this::attackAnimation));
        data.addAnimationController(newController("specialController", this::specialAnimation));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    private static AnimationBuilder newAnimation(String animationName, LoopType loopType){
        return new AnimationBuilder().addAnimation("animation." + animationName + "." + ENTITY_NAME, loopType.get());
    }
    private AnimationController newController(String name, AnimationController.IAnimationPredicate predicate){
        return new AnimationController(this, name, 0, predicate);
    }

    enum LoopType {
        LOOP(ILoopType.EDefaultLoopTypes.LOOP),
        PLAY_ONCE(ILoopType.EDefaultLoopTypes.PLAY_ONCE),
        HOLD_ON_LAST_FRAME(ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);

        private final ILoopType.EDefaultLoopTypes loopType;

        LoopType(ILoopType.EDefaultLoopTypes loop) {
            this.loopType = loop;
        }
        LoopType() {
            this.loopType = ILoopType.EDefaultLoopTypes.LOOP;
        }

        public ILoopType.EDefaultLoopTypes get() {
            return loopType;
        }
    }
}

package net.dain.hongozmod.entity.templates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.UUID;


@ApiStatus.Experimental
public abstract class Infected extends Monster implements IAnimatable, NeutralMob {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    protected static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    protected static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    protected int remainingPersistentAngerTime;
    protected UUID persistentAngerTarget;
    protected int ticksUntilNextAlert;

    protected boolean triedAlertAllies = false;
    protected int alertedAlliesAmount = 0;

    public static final double SEEK_SPEED_MODIFIER = 1.80d;
    public static final double AVOID_SPEED_MODIFIER = 1.00d;
    public static final double SCAPE_SPEED_MODIFIER = 1.00d;

    public static final boolean MUST_SEE_TARGET = true;


    public Infected(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, SEEK_SPEED_MODIFIER, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, AVOID_SPEED_MODIFIER));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, MUST_SEE_TARGET));
        // this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        // this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, MUST_SEE_TARGET));
        // this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Animal.class, MUST_SEE_TARGET));
        // this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));

    }

    public float customHurt(DamageSource pSource, float pAmount){
        float newDamage = pAmount;
        float damageMultiplier = 1.0f;
        float tierMultiplier = 0.5f;
        int itemTier = 0;

        if (pSource.getEntity() instanceof LivingEntity livingEntity){
            if(livingEntity.getMainHandItem().getItem() instanceof TieredItem item){
                itemTier = item.getTier().getLevel();
                tierMultiplier =
                        item instanceof SwordItem?  0.5f :
                        item instanceof AxeItem?    1.0f :
                        item instanceof HoeItem?    2.0f :
                        0;

                tierMultiplier *= EnchantmentHelper.getFireAspect(livingEntity);
            }
        }
        damageMultiplier += (itemTier * tierMultiplier) + 1.0f;
        damageMultiplier += pSource.isFire()? 2 : 0;
        newDamage *= damageMultiplier;

        return newDamage;
    }
    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        return super.hurt(pSource, this.customHurt(pSource, pAmount));
    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel)this.level, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        super.customServerAiStep();
    }

    protected Class<? extends Infected> getAngryAlertType(){
        return Infected.class;
    }
    protected Class<? extends Infected> getAvoidAlertType(){
        return null;
    }
    protected int getAlertRange(){
        return 128;
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
        }
        else {
            this.alertedAlliesAmount = 0;
            if (this.getTarget() != null && this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertOthers();
                triedAlertAllies = true;
            } else if (this.getTarget() == null) {
                triedAlertAllies = false;
            }
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }
    private void alertOthers() {
        double d0 = this.getAlertRange(); // this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        this.level.getEntitiesOfClass(this.getAngryAlertType(), aabb, EntitySelector.NO_SPECTATORS).stream()
                .filter((entity) -> { return entity != this; })
                .filter((entity) -> { return entity.getClass() != this.getAvoidAlertType(); })
                .filter((entity) -> { return entity.getTarget() == null; })
                .filter((entity) -> { return !entity.isAlliedTo(this.getTarget()); })
                .forEach((entity) -> {
                    entity.setTarget(this.getTarget());
                    alertedAlliesAmount++;
                });
    }

    public boolean alertedAllies(){
        return this.triedAlertAllies;
    }
    public int getAlertedAlliesAmount() {
        return this.alertedAlliesAmount;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addPersistentAngerSaveData(pCompound);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readPersistentAngerSaveData(this.level, pCompound);
    }

    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        remainingPersistentAngerTime = pTime;
    }
    @Override
    public int getRemainingPersistentAngerTime() {
        return remainingPersistentAngerTime;
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

    protected <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        if(this.swinging){
            return PlayState.STOP;
        }

        if(event.isMoving()){
            event.getController().setAnimation(getAnimation("walk", LoopType.LOOP));
        }
        else{
            event.getController().setAnimation(getAnimation("idle", LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }
    protected <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        if(this.swinging){
            event.getController().setAnimation(getAnimation("attack", LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
    }
    protected  <E extends IAnimatable> PlayState specialAnimation(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(getController("controller", this::normalAnimation));
        data.addAnimationController(getController("attackController", this::attackAnimation));
        data.addAnimationController(getController("specialController", this::specialAnimation));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public String getEntityName(){
        String className = this.getClass().getName().toLowerCase();
        String packageName = this.getClass().getPackageName().toLowerCase() + "."; //.replaceFirst("entity", "");

        return className.replaceFirst(packageName, "")
                .replaceFirst("entity", "")
                .replaceFirst("evo", "evolved_");
    }

    public AnimationBuilder getAnimation(String animationName, LoopType loopType){
        return AnimationHelper.newAnimation(getEntityName(), animationName, loopType);
    }

    public AnimationController getController(String controllerName, AnimationController.IAnimationPredicate predicate){
        return AnimationHelper.newController(this, controllerName, predicate);
    }

}

package net.dain.hongozmod.entity.templates;

import com.mojang.math.Vector3f;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

    public Infected(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public boolean canBeHurtBy(Entity pEntity){
        return  pEntity instanceof HunterEntity ||
                !(pEntity instanceof Infected ||
                (pEntity instanceof AreaEffectCloud aoeCloud && aoeCloud.getOwner() instanceof Infected) ||
                (pEntity instanceof Projectile projectile && projectile.getOwner() instanceof Infected));
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return  pEffectInstance.getEffect() != MobEffects.POISON &&
                super.canBeAffected(pEffectInstance);
    }

    public float customHurt(DamageSource pSource, float pAmount){
        if(!this.canBeHurtBy(pSource.getEntity())){
            return 0.0f;
        }

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
        float newAmount = this.customHurt(pSource, pAmount);
        return newAmount > 0 && super.hurt(pSource, newAmount);
    }

    public boolean customAddEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity){
        return !((!pEffectInstance.getEffect().isBeneficial()) &&
                this.canBeHurtBy(pEntity));
    }
    @Override
    public boolean addEffect(@NotNull MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
        return this.customAddEffect(pEffectInstance, pEntity) && super.addEffect(pEffectInstance, pEntity);
    }

    @Override
    protected void customServerAiStep() {
        this.updatePersistentAnger((ServerLevel)this.level, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        super.customServerAiStep();
    }

    public Class<? extends Infected> getAngryAlertType(){
        return Infected.class;
    }
    public Class<? extends Infected> getAvoidAlertType(){
        return null;
    }
    public double getAlertRange(){
        return (int) this.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
    protected int getAlertTicks(){
        return ALERT_INTERVAL.sample(this.random);
    }
    public boolean customAlertFilter(Entity entity){
        return true;
    }

    protected final void maybeAlertOthers() {
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
            this.ticksUntilNextAlert = this.getAlertTicks();
        }
    }
    protected void alertOthers() {
        if (this.getTarget() == null){
            return;
        }

        double len = this.getAlertRange();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(len, len, len);
        this.level.getEntitiesOfClass(this.getAngryAlertType(), aabb, EntitySelector.NO_SPECTATORS).stream()
                .filter((entity) -> entity != this)
                .filter(this::customAlertFilter)
                .filter((entity) -> entity.getClass() != this.getAvoidAlertType())
                .filter((entity) -> entity.getTarget() == null)
                .filter((entity) -> !entity.isAlliedTo(this.getTarget()))
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
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addPersistentAngerSaveData(pCompound);
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
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
        if(!this.canBeHurtBy(pTarget)){
            return;
        }

        if (this.getTarget() == null && pTarget != null) {
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
        if (pTarget instanceof Player) {
            this.setLastHurtByPlayer((Player)pTarget);
        }

        super.setTarget(pTarget);
    }

    protected void addParticlesAroundSelf(ParticleOptions pParticleOption, int amount) {
        for(int i = 0; i < amount; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(pParticleOption, this.getRandomX(1.0D), this.getRandomY() + 0.75D, this.getRandomZ(1.0D), d0, d1, d2);
        }

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
        String packageName = this.getClass().getPackageName().toLowerCase() + ".";

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

    public Vector3f getModelScale(){
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public float getShadowRadius(){
        return 1.0f;
    }

}

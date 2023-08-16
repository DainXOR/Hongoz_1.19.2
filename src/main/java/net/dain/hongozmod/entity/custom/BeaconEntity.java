package net.dain.hongozmod.entity.custom;

import com.mojang.blaze3d.shaders.Effect;
import com.mojang.logging.LogUtils;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class BeaconEntity extends Infected {
    public static final int TICKS_TO_REGENERATE = 20 * 3;
    public static final int REGENERATION_AMOUNT = 5;

    public static final int TICKS_TO_THROW = 20 * 60;
    public static final int AGGRESSIVE_TICKS_TO_THROW = 20 * 10;

    public static final int MIN_WAIT_TO_THROW = 20;
    public static final int MAX_CONSECUTIVE_THROWS = 3;

    public static final int MIN_ATTACK_HEALTH = 50;
    public static final int AGGRESSIVE_THROW_HEALTH_LOSS = 10;

    public static final int PANIC_ATTACK_HEALTH_LOSS = 50;
    public static final int PANIC_TARGET_DISTANCE = 16;
    public static final int PANIC_COOLDOWN = 20 * 60;

    private int regenerateTimer = 0;

    private int throwTimer = 0;
    private int consecutiveThrows = 0;

    private int panicTimer = PANIC_COOLDOWN;

    private boolean isSpecialAttacking = false;


    public BeaconEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 50;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.00)
                .add(Attributes.ATTACK_DAMAGE, 3.00)
                .add(Attributes.MOVEMENT_SPEED, 0.00)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 0.0d, false));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));

        super.registerGoals();
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    public void knockback(double pStrength, double pX, double pZ) {

    }
    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        return  (pSource.isProjectile() && this.random.nextBoolean()) ||
                super.hurt(pSource, pAmount * 0.5f);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return  pEffectInstance.getEffect() != MobEffects.HARM &&
                super.canBeAffected(pEffectInstance);
    }

    @Override
    public void tick() {
        super.tick();

        if(!this.level.isClientSide()){
            this.tryRegenerate();
            this.maybeThrowEgg();
            this.maybePanic();
        }
    }

    public boolean needToRegenerate(){
        return this.getHealth() < this.getMaxHealth();
    }
    public boolean canRegenerate(){
        return !this.isAggressive() && this.regenerateTimer >= TICKS_TO_REGENERATE;
    }
    public void regenerate(){
        this.heal(REGENERATION_AMOUNT);
        this.sendPacket((byte) 61);
    }
    public void tryRegenerate(){
        this.regenerateTimer++;
        if(this.needToRegenerate() && this.canRegenerate()) {
            regenerate();
            this.regenerateTimer = 0;
        }
    }

    public boolean canThrow(){
        return  !this.isThrowingEgg() &&
                this.getHealth() >= MIN_ATTACK_HEALTH;
    }
    public boolean canAggressiveThrow(){
        return  this.isAggressive() &&
                this.throwTimer >= AGGRESSIVE_TICKS_TO_THROW &&
                this.getTarget() != null &&
                this.distanceToSqr(this.getTarget()) <= getAttributeValue(Attributes.FOLLOW_RANGE);
    }
    public boolean canNormalThrow(){
        return  this.consecutiveThrows > 0 ||
                (
                !this.isAggressive() &&
                !needToRegenerate() &&
                this.throwTimer >= TICKS_TO_THROW
                );
    }

    public boolean isThrowingEgg(){
        return this.throwTimer <= MIN_WAIT_TO_THROW;
    }

    public void maybeThrowEgg(){
        if(this.level.isClientSide()){
            return;
        }

        if(this.canThrow()){
            if(canNormalThrow()){
                this.throwEgg();
                this.throwTimer = 0;
                this.consecutiveThrows++;

                if(this.consecutiveThrows >= MAX_CONSECUTIVE_THROWS)
                    this.consecutiveThrows = 0;
            }
            else if(canAggressiveThrow()){
                this.throwEggAt(this.getTarget());
                this.setHealth(this.getHealth() - AGGRESSIVE_THROW_HEALTH_LOSS);
                this.throwTimer = 0;
                this.consecutiveThrows = 0;
            }
        }
        this.throwTimer++;
    }

    public void throwEgg(){
        double dX = (this.random.nextFloat() * 1.5) - 0.75;
        double dZ = (this.random.nextFloat() * 1.5) - 0.75;

        this.throwEggAt(dX, 0.5, dZ);
    }
    public void throwEggAt(LivingEntity target){
        double dX = target.getX() - this.getX();
        double dZ = target.getZ() - this.getZ();

        double norm = 1 / Math.sqrt((dX * dX) + (dZ * dZ));
        double normX = dX * norm;
        double normZ = dZ * norm;

        this.throwEggAt(normX, 0.5, normZ);
    }
    public void throwEggAt(double pX, double pY, double pZ){
        FungiEgg egg = ModEntityTypes.FUNGI_EGG.get().create(this.level);
        assert egg != null;

        egg.moveTo(Vec3.atCenterOf(this.blockPosition()));
        egg.setDeltaMovement(pX, pY, pZ);
        egg.setInnerEntity(this.random.nextFloat() >= 0.8f? ModEntityTypes.MAGGOT.get() : ModEntityTypes.ZHONGO.get());

        this.level.addFreshEntity(egg);

        this.playSound(SoundEvents.EGG_THROW, 1.0f, 0.01f);
        this.sendPacket((byte) 62);
    }

    public boolean shouldPanic(){
        return  this.getTarget() != null &&
                this.distanceToSqr(this.getTarget()) <= PANIC_TARGET_DISTANCE;
    }
    public boolean canPanic(){
        return  this.isAggressive() &&
                this.getHealth() - PANIC_ATTACK_HEALTH_LOSS >= MIN_ATTACK_HEALTH &&
                this.panicTimer >= PANIC_COOLDOWN;
    }
    public void maybePanic(){
        this.panicTimer++;
        if(this.canPanic() && this.shouldPanic()){
            this.panicTimer = 0;
            this.panic();
            this.setHealth(this.getHealth() - PANIC_ATTACK_HEALTH_LOSS);
        }
    }
    public void panic(){
        AreaEffectCloud poisonCloud = EntityType.AREA_EFFECT_CLOUD.create(this.level);
        assert poisonCloud != null;

        MobEffectInstance effect_1 = new MobEffectInstance(MobEffects.HARM);
        MobEffectInstance effect_2 = new MobEffectInstance(MobEffects.POISON, 20 * 30, 1);
        MobEffectInstance effect_3 = new MobEffectInstance(MobEffects.BLINDNESS, 20 * 30, 0);

        poisonCloud.addEffect(effect_1);
        poisonCloud.addEffect(effect_2);
        poisonCloud.addEffect(effect_3);

        poisonCloud.setRadius(5.0f);
        poisonCloud.setRadiusPerTick(-5.0f / (20.0f * 30.0f));
        poisonCloud.setRadiusOnUse(-0.05f);
        poisonCloud.setOwner(this);
        poisonCloud.moveTo(Vec3.atCenterOf(this.blockPosition()));

        this.level.addFreshEntity(poisonCloud);
        this.sendPacket((byte) 62);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.SLIME_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }
    @Override
    public void handleEntityEvent(byte pId) {
        switch (pId) {
            case 61 -> this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER, 20);
            case 62 -> this.isSpecialAttacking = true;
            default -> super.handleEntityEvent(pId);
        }
    }

    @Override
    protected <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        if(this.swinging || this.isSpecialAttacking){
            return PlayState.STOP;
        }

        event.getController().setAnimation(getAnimation("idle", LoopType.LOOP));
        return PlayState.CONTINUE;
    }
    @Override
    protected <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        if(this.swinging ^ this.isSpecialAttacking){
            event.getController().setAnimation(getAnimation("attack", LoopType.PLAY_ONCE));
            event.getController().setAnimationSpeed(6.0);

            this.isSpecialAttacking = event.getController().getAnimationState() != AnimationState.Stopped;

            return PlayState.CONTINUE;
        }

        event.getController().markNeedsReload();
        return PlayState.STOP;
    }

    @Override
    public float getShadowRadius() {
        return 0.8f;
    }

    private void sendPacket(byte value){
        ServerLevel serverLevel = (ServerLevel) this.level;
        ClientboundEntityEventPacket entityEventPacket = new ClientboundEntityEventPacket((Entity) this, value);
        ServerChunkCache scc = serverLevel.getChunkSource();
        scc.broadcastAndSend(this, entityEventPacket);
    }
}

package net.dain.hongozmod.entity.custom;

import com.mojang.logging.LogUtils;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class BeaconEntity extends Infected {
    public static final int TICKS_TO_REGENERATE = 20 * 5;
    public static final int REGENERATION_AMOUNT = 5;

    public static final int TICKS_TO_THROW = 20 * 10;
    public static final int AGGRESSIVE_TICKS_TO_THROW = 20 * 2;

    public static final int MIN_WAIT_TO_THROW = 20;
    public static final int MAX_CONSECUTIVE_THROWS = 3;

    private int regenerateTimer = 0;

    private int throwTimer = 0;
    private int minThrowWaitTimer = 0;
    private int consecutiveThrows = 0;

    public BeaconEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 50;
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

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));

    }

    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    public void knockback(double pStrength, double pX, double pZ) {

    }

    @Override
    public void tick() {
        super.tick();
        final float lastHp = this.getHealth();

        if(!this.level.isClientSide()){
            this.tryRegenerate();
            this.maybeThrowEgg();
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
    }
    public void tryRegenerate(){
        this.regenerateTimer++;
        if(this.needToRegenerate() && this.canRegenerate()) {
            regenerate();
            this.regenerateTimer = 0;

            ServerLevel serverLevel = (ServerLevel) this.level;
            ClientboundEntityEventPacket entityEventPacket = new ClientboundEntityEventPacket((Entity) this, (byte) 61);
            ServerChunkCache scc = serverLevel.getChunkSource();
            scc.broadcastAndSend(this, entityEventPacket);
        }
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if(pId == 61){
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER, 20);
        } else {
            super.handleEntityEvent(pId);
        }
    }

    public boolean canAggressiveThrow(){
        return  this.isAggressive() && this.throwTimer >= AGGRESSIVE_TICKS_TO_THROW;
    }
    public boolean canNormalThrow(){
        return this.throwTimer >= TICKS_TO_THROW;
    }

    public boolean isThrowingEgg(){
        return this.minThrowWaitTimer > 0;
    }
    public void maybeThrowEgg(){
        if(this.level.isClientSide()){
            return;
        }

        if(this.isThrowingEgg()){
            this.minThrowWaitTimer++;
            if(this.minThrowWaitTimer >= MIN_WAIT_TO_THROW){
                this.minThrowWaitTimer = 0;
            }
        }
        else {
            this.throwTimer++;

            if(canAggressiveThrow()){
                if (this.needToRegenerate() && this.isAggressive() && this.getTarget() != null) {
                    this.throwEggAt(this.getTarget());

                } else if(!this.isThrowingEgg() && this.consecutiveThrows < MAX_CONSECUTIVE_THROWS){
                    this.throwEgg();
                    this.consecutiveThrows++;
                }

                this.throwTimer = 0;
                this.minThrowWaitTimer = 1;
            }
        }
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

        this.level.addFreshEntity(egg);

        this.playSound(SoundEvents.EGG_THROW, 1.0f, 0.1f);
        //this.playSound(SoundEvents.TURTLE_LAY_EGG, 1.0f, 0.1f);
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
    protected <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        if(this.swinging || this.isThrowingEgg()){
            return PlayState.STOP;
        }
        event.getController().setAnimation(getAnimation("idle", LoopType.LOOP));
        return PlayState.CONTINUE;
    }
    @Override
    protected <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        if(this.swinging ^ this.isThrowingEgg()){
            event.getController().setAnimation(getAnimation("attack", LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
    }
    @Override
    protected <E extends IAnimatable> PlayState specialAnimation(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public float getShadowRadius() {
        return 0.8f;
    }
}

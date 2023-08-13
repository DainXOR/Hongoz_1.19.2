package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ZhongoEntity extends Infected {

    public ZhongoEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 15;
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.00)
                .add(Attributes.ATTACK_DAMAGE, 3.00)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.44d, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public Class<? extends Infected> getAvoidAlertType() {
        return HunterEntity.class;
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

    @Override
    public float getShadowRadius() {
        return 0.3f;
    }
}

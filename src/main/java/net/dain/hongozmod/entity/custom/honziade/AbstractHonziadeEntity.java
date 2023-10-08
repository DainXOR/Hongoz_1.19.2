package net.dain.hongozmod.entity.custom.honziade;

import com.mojang.math.Vector3f;
import net.dain.hongozmod.colony.Colony;
import net.dain.hongozmod.colony.role.ColonyMember;
import net.dain.hongozmod.colony.role.ColonyQueen;
import net.dain.hongozmod.colony.role.ColonyRoles;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractHonziadeEntity extends Infected implements IAnimatable, ColonyMember {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(AbstractHonziadeEntity.class, EntityDataSerializers.BYTE);
    public static final int QUEENING_AGE = 20 * 60 * 5;
    protected HonziadeColony colony = null;
    protected ColonyRoles role = ColonyRoles.NONE;

    protected int age = 0;

    public AbstractHonziadeEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(3, new AbstractHonziadeEntity.HonziadeAttackGoal(this, 1.3d));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));

    }


    @Contract("null->false") @Override
    public boolean setColony(@Nullable Colony newColony){
        if(newColony instanceof HonziadeColony hc){
            this.colony = hc;
            return true;
        }
        return false;
    }
    @Override
    public HonziadeColony getColony() {
        return this.colony;
    }

    @Override
    public void setRole(ColonyRoles role) {
        this.role = role;
    }

    @Override
    public ColonyRoles getRole() {
        return this.role;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
    }

    public Class<? extends Infected> getAngryAlertType() {
        return HonziadeEntity.class;
    }
    @Override
    public double getAlertRange() {
        return 1500.0d;
    }

    @Override
    protected void alertOthers() {


    }

    void addHonziadeSaveData(CompoundTag pNbt) {
        pNbt.putInt("HonziadeAge", this.age);

    }
    void readHonziadeSaveData(CompoundTag pTag) {
        this.age = pTag.getInt("HonziadeAge");

    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addHonziadeSaveData(pCompound);
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readHonziadeSaveData(pCompound);
    }

    private static <T extends Entity> T getEntityFromTag(@NotNull CompoundTag tag, @NotNull Level level, @NotNull String key){
        ServerLevel serverLevel = (ServerLevel) level;
        UUID entityUUID = tag.getUUID(key);
        return (T) serverLevel.getEntity(entityUUID);
    }

    public @NotNull MobType getMobType() {
        return MobType.ARTHROPOD;
    }
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }
    protected boolean canAddPassenger(@NotNull Entity pPassenger) {
        return false;
    }

    protected float getStandingEyeHeight(@NotNull Pose pPose, @NotNull EntityDimensions pSize) {
        return 0.4F;
    }

    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        return new WallClimberNavigation(this, pLevel);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.setClimbing(this.horizontalCollision);
            this.honziadeAging();
        }
    }

    public void makeStuckInBlock(BlockState pState, @NotNull Vec3 pMotionMultiplier) {
        if (!pState.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(pState, pMotionMultiplier);
        }

    }

    public boolean onClimbable() {
        return this.isClimbing();
    }
    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }
    public void setClimbing(boolean pClimbing) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pClimbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    boolean canBecomeQueen(){
        return (
                !this.isAggressive() &&
                        this.age >= QUEENING_AGE &&
                        this.getHealth() == this.getMaxHealth()
        ) || (
                this.isAggressive() &&
                        this.alertedAllies() &&
                        this.getAlertedAlliesAmount() == 0 &&
                        this.getHealth() <= this.getMaxHealth() * 0.75f
        );

    }
    public ColonyQueen becomeQueen(){
        HonziadeQueen newQueen = new HonziadeQueen(ModEntityTypes.HONZIADE_QUEEN.get(), this.level);
        newQueen.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));

        if(!newQueen.checkSpawnObstruction(this.level)){
            newQueen.discard();
            return null;
        }

        newQueen.setNoAi(this.isNoAi());
        if (this.hasCustomName()) {
            newQueen.setCustomName(this.getCustomName());
            newQueen.setCustomNameVisible(this.isCustomNameVisible());
        }
        newQueen.setPersistenceRequired();
        float healthRatio = this.getHealth() / this.getMaxHealth();
        healthRatio *= (float)this.age / (float) QUEENING_AGE;
        newQueen.setHealth(newQueen.getMaxHealth() * healthRatio);

        newQueen.setTarget(this.getTarget());
        this.setTarget(null);

        net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, newQueen);
        this.level.addFreshEntity(newQueen);
        this.discard();
        return newQueen;
    }

    protected void honziadeAging(){
        this.age++;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        if(pEntity instanceof LivingEntity entity){
            this.setTarget(entity);
        }
        return super.doHurtTarget(pEntity);
    }

    @Override
    public <NewRoleClass extends ColonyMember> NewRoleClass changeRole(ColonyRoles newRole) {
        if(this.getRole() == newRole){
            return (NewRoleClass) this;
        }

        switch (newRole){
            case WORKER,FOOD_SACK,HEIR -> {
                HonziadeWorker a = new HonziadeWorker(ModEntityTypes.HONZIADE.get(), this.level);
                a.setRole(newRole);

                return (NewRoleClass) a;
            }
            case EXPLORER -> new HonziadeExplorer(ModEntityTypes.HONZIADE.get(), this.level);
            case WARRIOR -> new HonziadeWarrior(ModEntityTypes.HONZIADE.get(), this.level);
            case ROYAL_WARRIOR -> new HonziadeRoyalWarrior(ModEntityTypes.HONZIADE.get(), this.level);
        }
        return null;
    }

    protected void playStepSound(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 1.0F, 1.0F);
    }
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    public Vector3f getModelScale() {
        return new Vector3f(0.8f, 0.8f, 0.8f);
    }

    public static class HonziadeAttackGoal extends MeleeAttackGoal {

        public HonziadeAttackGoal(AbstractHonziadeEntity pHonziade, double pSpeedModifier) {
            super(pHonziade, pSpeedModifier, true);
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget) {
            return this.mob.getBbWidth() + pAttackTarget.getBbWidth() + 7.0;
        }
    }
}

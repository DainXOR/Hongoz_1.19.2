package net.dain.hongozmod.entity.custom.honziade;

import com.mojang.math.Vector3f;
import net.dain.hongozmod.colony.AlertLevel;
import net.dain.hongozmod.colony.Colony;
import net.dain.hongozmod.colony.role.ColonyMember;
import net.dain.hongozmod.colony.role.ColonyQueen;
import net.dain.hongozmod.colony.role.ColonyRoles;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class HonziadeQueen extends AbstractHonziadeEntity implements ColonyQueen {
    public static final UniformInt BIRTH_AMOUNT = UniformInt.of(2, 8);
    public static final float MIN_REINFORCEMENT_WAIT_PROGRESS = 0.25f;
    public static final int REINFORCEMENTS_COOLDOWN = 20 * 60 * 2;
    public static final int CHILDREN_LIMIT = 30;
    public static final int ADOPT_LIMIT = 20;

    protected final int workerPersonalChance;
    protected final int ExplorerPersonalChance;
    protected final int WarriorPersonalChance;
    protected final int RoyalWarriorPersonalChance;

    protected final int HeirChance;
    protected final int FoodStoreChance;
    protected final float RejectChance;

    public int REINFORCEMENTS_TICKS = 0;
    protected HonziadeColony colony;

    {
        this.workerPersonalChance = UniformInt.of(50, 150).sample(this.random);
        this.ExplorerPersonalChance = UniformInt.of(50, 100).sample(this.random);
        this.WarriorPersonalChance = UniformInt.of(20, 80).sample(this.random);
        this.RoyalWarriorPersonalChance = UniformInt.of(10, 50).sample(this.random);

        this.HeirChance = this.random.nextInt() % 5;
        this.FoodStoreChance = this.random.nextInt() % 20;
        this.RejectChance = (float) this.random.nextGaussian();
    }

    public HonziadeQueen(EntityType<? extends HonziadeEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 75;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.00)
                .add(Attributes.ATTACK_DAMAGE, 5.00)
                .add(Attributes.ATTACK_SPEED, 0.10)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 256.00)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.50)
                .build();
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 256));
        this.goalSelector.addGoal(4, new AbstractHonziadeEntity.HonziadeAttackGoal(this, 1.3d));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new AvoidEntityGoal<>(this, Warden.class, 64.0f, 1.3f, 1.5f));
        this.targetSelector.addGoal(2, new AvoidEntityGoal<>(this, AbstractGolem.class, 64.0f, 1.3f, 1.5f));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));
    }
    @Contract("null->false")
    @Override
    public boolean setColony(@Nullable Colony newColony){
        if(newColony instanceof HonziadeColony nc){
            this.colony = nc;
            return true;
        }
        else if(this.colony == null) {
            this.colony = new HonziadeColony(this);
        }

        return false;
    }

    public boolean checkSpawnObstruction(@NotNull LevelReader pLevel) {
        return super.checkSpawnObstruction(pLevel) && pLevel.noCollision(this, this.getType().getDimensions().makeBoundingBox(this.position()));
    }
    public boolean requestAdoption(@NotNull HonziadeEntity entity){
        return false;
    }

    @Override
    protected int getAlertTicks() {
        return 20 * 30;
    }
    public final void alertColony() {
        if (this.getTarget() == null){
            return;
        }

        this.playRoarSound();
        List<AbstractHonziadeEntity> children = this.getColony().getMembers();
        children.removeIf(Objects::isNull);
        children.stream()
                .filter((child) -> child.getTarget() == null)
                .forEach((child) -> {
                    child.setTarget(this.getTarget());
                });
    }

    public boolean canLayEggs(float waitProgress){
        if(REINFORCEMENTS_TICKS >= REINFORCEMENTS_COOLDOWN){
            return true;
        }

        boolean surpassThreshold = waitProgress >= MIN_REINFORCEMENT_WAIT_PROGRESS;
        boolean randomComponent = this.random.nextFloat() + waitProgress >= 0.95f;

        return this.isAggressive() && surpassThreshold && randomComponent;
    }
    public boolean maybeLayEggs(){
        float waitProgress = (float)REINFORCEMENTS_TICKS / (float)REINFORCEMENTS_COOLDOWN;

        if(this.canLayEggs(waitProgress) && this.getColony().maxCapacity > this.getColony().completeSize()){
            int reinforcementCount = this.isAggressive()?
                    (int)(BIRTH_AMOUNT.getMinValue() * (1 + waitProgress)) :
                    BIRTH_AMOUNT.sample(this.random);

            return this.layEggs(reinforcementCount);
        }
        return false;
    }
    @Contract(value = "_->true")
    public boolean layEggs(int count){
        for (int i = 0; i <= count; i++){
            HonziadeEgg child = new HonziadeEgg(ModEntityTypes.HONZIADE.get(), this.level);// = ModEntityTypes.HONZIADE.get().create(this.level);

            child.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
            child.setPersistenceRequired();
            this.getColony().addNewMember(child, false);

            this.level.addFreshEntity(child);
        }

        return true;
    }

    public void tick() {
        super.tick();
        REINFORCEMENTS_TICKS++;

        if(this.maybeLayEggs()){
            REINFORCEMENTS_TICKS = 0;
        }
    }

    @Override
    public ColonyRoles getRole() {
        return ColonyRoles.QUEEN;
    }

    @Override
    public <NewRoleClass extends ColonyMember> NewRoleClass changeRole(ColonyRoles newRole) {
        return null;
    }

    @Override
    public void returnToQueen(@NotNull AlertLevel priority) {
        return;
    }


    @Override
    public boolean requestAdoption(@NotNull ColonyMember entity) {
        return entity instanceof AbstractHonziadeEntity e && this.getColony().requestAdoption(e);
    }
    @Override
    public int getWorkerChance() {
        return this.workerPersonalChance;
    }
    @Override
    public int getExplorerChance() {
        return this.ExplorerPersonalChance;
    }
    @Override
    public int getWarriorChance() {
        return this.WarriorPersonalChance;
    }
    @Override
    public int getRoyalWarriorChance() {
        return this.RoyalWarriorPersonalChance;
    }

    @Override
    public int getHeirChance() {
        return this.HeirChance;
    }
    @Override
    public int getFoodStoreChance() {
        return this.FoodStoreChance;
    }

    @Override
    public float getRejectChance() {
        return this.RejectChance;
    }

    @Override
    public int getBirthAmount() {
        return 0;
    }

    @Override
    public int getMinBirthCooldown() {
        return 0;
    }

    @Override
    public int getBirthCooldown() {
        return 0;
    }

    @Override
    public void callProtection() {

    }

    @Override
    public void callColony() {

    }

    @Override
    public void forceCallColony() {

    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    protected float getStandingEyeHeight(@NotNull Pose pPose, @NotNull EntityDimensions pSize) {
        return 1.0F;
    }

    public void onAddedToWorld() {
        super.onAddedToWorld();
    }
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
    }

    protected void playStepSound(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 3.0F, 0.2F);
    }
    protected void playRoarSound(){
        this.playSound(SoundEvents.SPIDER_HURT, 8.0F, 2.5F);
        this.playSound(SoundEvents.CREEPER_HURT, 8.0F, 1.5F);

        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 4.0F);

        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 1.0F);

        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_HURT, 8.0F, 0.5F);
    }

    protected SoundEvent getAmbientSound() {
        return ModSounds.HONZIADE_AMBIENT.get();
    }
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        this.playRoarSound();
        return SoundEvents.HOSTILE_HURT;
    }
    protected SoundEvent getDeathSound() {
        this.playSound(SoundEvents.SPIDER_DEATH, 8.0F, 2.5F);
        this.playSound(SoundEvents.CREEPER_DEATH, 8.0F, 1.5F);

        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 4.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 4.0F);

        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 1.0F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 1.0F);

        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 0.5F);
        this.playSound(SoundEvents.GHAST_DEATH, 8.0F, 0.5F);

        return SoundEvents.HOSTILE_DEATH;
    }

    public String getEntityName(){
        return "honziade";
    }

    @Override
    public Vector3f getModelScale() {
        return new Vector3f(2.5f, 2.5f, 2.5f);
    }
    @Override
    public float getShadowRadius() {
        return 2.0f;
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }
}

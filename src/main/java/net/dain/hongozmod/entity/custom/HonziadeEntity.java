package net.dain.hongozmod.entity.custom;

import com.mojang.math.Vector3f;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
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
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HonziadeEntity extends Infected implements IAnimatable{
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(HonziadeEntity.class, EntityDataSerializers.BYTE);
    private static final List<Queen> activeQueens = new ArrayList<>(5);
    private static boolean crowningInProgress = false;
    private static HonziadeEntity crowned = null;

    public static final int SUITABLE_AGE = 20 * 60 * 5;

    private Queen parent = null;
    public int age = 0;

    public HonziadeEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 25;

        this.setParent(this.getClosestQueen().getA());
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.00)
                .add(Attributes.ATTACK_DAMAGE, 1.00)
                .add(Attributes.ATTACK_SPEED, 2.00)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.FOLLOW_RANGE, 128.00)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.50)
                .build();
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(3, new HonziadeEntity.HonziadeAttackGoal(this, 1.3d));
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

    public void setParent(@NotNull Queen parent){
        this.parent = parent;
    }
    public @Nullable Queen getParent(){ return this.parent; }
    public boolean hasParent() { return this.getParent() != null && !this.getParent().isDeadOrDying(); }

    public Class<? extends Infected> getAngryAlertType() {
        return HonziadeEntity.class;
    }
    @Override
    public double getAlertRange() {
        return super.getAlertRange() * 1.5;
    }

    @Override
    protected void alertOthers() {
        if (this.getTarget() == null || !this.hasParent()){
            return;
        }

        List<HonziadeEntity> siblings = this.getParent().children;
        List<HonziadeEntity> alertableSiblings = siblings.stream()
                .filter(sibling -> sibling != this)
                .filter(sibling -> sibling.distanceToSqr(this) <= this.getAlertRange())
                .filter(sibling -> sibling.getTarget() == null)
                .toList();


        alertableSiblings.forEach(sibling -> {
                    sibling.setTarget(this.getTarget());
                    alertedAlliesAmount++;
                });
        alertedAlliesAmount = alertableSiblings.size();
    }

    void addHonziadeAgeSaveData(CompoundTag pNbt) {
        pNbt.putInt("HonziadeAge", this.age);
    }
    void readHonziadeAgeSaveData(CompoundTag pTag) {
        this.age = pTag.getInt("HonziadeAge");
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addHonziadeAgeSaveData(pCompound);

        if(this.getParent() != null){
            pCompound.putUUID("parent", this.getParent().uuid);
        }
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readHonziadeAgeSaveData(pCompound);

        if(pCompound.contains("parent")) {
            ServerLevel level = (ServerLevel) this.level;
            UUID entityUUID = pCompound.getUUID("parent");

            if (level.getEntity(entityUUID) instanceof Queen parentEntity) {
                this.setParent(parentEntity);
            }
        }

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

    Pair<Queen, Double> getClosestQueen(){
        if(activeQueens.isEmpty()){
            return new Pair<>(null, 100_000.0);
        } else if (this.isQueen()) {
            return new Pair<>((Queen) this, 0.0);
        }

        double closestDistance = 100_000;
        Queen closestQueen = null;

        for (Queen queen : activeQueens) {
            double currentDistance = this.distanceToSqr(queen);
            if (currentDistance <= closestDistance) {
                closestDistance = currentDistance;
                closestQueen = queen;
            }
        }
        return new Pair<>(closestQueen, closestDistance);
    }
    boolean isQueen(){
        return false;
    }

    boolean canBecomeQueen(){
        boolean b = this.hasParent() &&
                this.distanceToSqr(this.getParent()) > this.getAlertRange() * 1.5f;

        return  (this.getClosestQueen().getB() > this.getAlertRange() * 1.5f) &&
                    ((this.age >= SUITABLE_AGE && this.getHealth() == this.getMaxHealth() && !this.isAggressive()) ||
                     (this.isAggressive() && this.alertedAllies() && this.getAlertedAlliesAmount() == 0 && this.getHealth() <= this.getMaxHealth() * 0.75f));
    }
    boolean tryBecomeQueen(){
        boolean succeeded = false;
        if(this.canBecomeQueen()){
            HonziadeEntity.stopCrownings(this);
            if (crowned == this){
                succeeded = this.becomeQueen() != null;
            }
            HonziadeEntity.resumeCrownings();
        }
        return succeeded;
    }
    Queen becomeQueen(){
        Queen newQueen = ModEntityTypes.HONZIADE_QUEEN.get().create(this.level);
        assert newQueen != null;
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
        healthRatio *= (float)this.age / (float)SUITABLE_AGE;
        newQueen.setHealth(newQueen.getMaxHealth() * healthRatio);

        newQueen.setTarget(this.getTarget());
        this.setTarget(null);

        net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, newQueen);
        this.level.addFreshEntity(newQueen);
        this.discard();
        return newQueen;
    }

    protected static void stopCrownings(HonziadeEntity crownedEntity){
        crowningInProgress = true;
        crowned = crownedEntity;
    }
    protected static void resumeCrownings(){
        crowningInProgress = false;
        crowned = null;
    }
    protected static boolean crowningInProgress(){
        return crowningInProgress;
    }

    protected void honziadeAging(){
        this.age++;

        if(!this.isQueen() && this.isAlive() && !HonziadeEntity.crowningInProgress()) {
            if(this.tryBecomeQueen()){
            }
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        if(pEntity instanceof LivingEntity entity){
            this.setTarget(entity);
        }
        return super.doHurtTarget(pEntity);
    }

    protected void playStepSound(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 1.0F, 1.5F);
    }
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        this.playSound(SoundEvents.GHAST_HURT, 0.4F, 7.0F);
        this.playSound(SoundEvents.CREEPER_HURT, 0.5F, .75F);
        this.playSound(SoundEvents.SPIDER_HURT, 1.0F, 2.0F);

        return SoundEvents.HOSTILE_HURT;
    }
    protected SoundEvent getDeathSound() {
        this.playSound(SoundEvents.GHAST_DEATH, 0.4F, 7.0F);
        this.playSound(SoundEvents.CREEPER_DEATH, 0.5F, 0.75F);
        this.playSound(SoundEvents.SPIDER_DEATH, 1.0F, 2.0F);

        return SoundEvents.HOSTILE_DEATH;
    }

    @Override
    public Vector3f getModelScale() {
        return new Vector3f(0.8f, 0.8f, 0.8f);
    }

    public static class Queen extends HonziadeEntity {
        public static final UniformInt REINFORCEMENTS_AMOUNT = UniformInt.of(2, 8);
        public static final float MIN_REINFORCEMENT_WAIT_PROGRESS = 0.25f;
        public static final int REINFORCEMENTS_COOLDOWN = 20 * 60 * 2;

        private final List<HonziadeEntity> children = new ArrayList<>(10);

        public int REINFORCEMENTS_TICKS = 0;


        public Queen(EntityType<? extends HonziadeEntity> pEntityType, Level pLevel) {
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
            this.goalSelector.addGoal(4, new HonziadeAttackGoal(this, 1.3d));
            this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

            this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
            this.targetSelector.addGoal(1, new AvoidEntityGoal<>(this, Warden.class, 64.0f, 1.3f, 1.5f));
            this.targetSelector.addGoal(2, new AvoidEntityGoal<>(this, AbstractGolem.class, 64.0f, 1.3f, 1.5f));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
            this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
            this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));
        }

        public boolean checkSpawnObstruction(@NotNull LevelReader pLevel) {
            return super.checkSpawnObstruction(pLevel) && pLevel.noCollision(this, this.getType().getDimensions().makeBoundingBox(this.position()));
        }

        @Override
        protected int getAlertTicks() {
            return 20 * 30;
        }

        protected final void alertOthers() {
            if (this.getTarget() == null){
                return;
            }

            this.playSound(SoundEvents.SPIDER_HURT, 3.0F, 1.5F);
            this.playSound(SoundEvents.GHAST_HURT, 5.0F, 1.5F);
            this.playSound(SoundEvents.CREEPER_HURT, 3.0F, 1.5F);

            this.children.stream()
                    .filter((entity) -> entity.getTarget() == null)
                    .forEach((entity) -> {
                entity.setTarget(this.getTarget());
            });
        }

        public boolean canSpawnReinforcements(float waitProgress){

            if(REINFORCEMENTS_TICKS >= REINFORCEMENTS_COOLDOWN){
                return true;
            }

            boolean surpassThreshold = waitProgress >= MIN_REINFORCEMENT_WAIT_PROGRESS;
            boolean randomComponent = this.random.nextFloat() + waitProgress >= 0.95f;

            return this.isAggressive() && surpassThreshold && randomComponent;
        }
        public boolean maybeSpawnReinforcements(){
            float waitProgress = (float)REINFORCEMENTS_TICKS / (float)REINFORCEMENTS_COOLDOWN;

            if(this.canSpawnReinforcements(waitProgress)){
                int reinforcementCount = this.isAggressive()?
                        (int)(REINFORCEMENTS_AMOUNT.getMinValue() * (1 + waitProgress)) :
                        REINFORCEMENTS_AMOUNT.sample(this.random);

                return this.spawnReinforcements(reinforcementCount);
            }
            return false;
        }
        public boolean spawnReinforcements(int count){
            boolean succeeded = true;

            for (int i = 0; i <= count; i++){
                HonziadeEntity child = ModEntityTypes.HONZIADE.get().create(this.level);
                assert child != null;
                child.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
                child.setParent(this);
                child.setPersistenceRequired();
                this.children.add(child);

                succeeded = succeeded && this.level.addFreshEntity(child);
            }

            return succeeded;
        }

        public void tick() {
            super.tick();
            REINFORCEMENTS_TICKS++;

            if(this.maybeSpawnReinforcements()){
                REINFORCEMENTS_TICKS = 0;
            }

            this.children.removeIf(LivingEntity::isDeadOrDying);

        }

        boolean isQueen() {
            return true;
        }
        public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
            return false;
        }

        protected float getStandingEyeHeight(@NotNull Pose pPose, @NotNull EntityDimensions pSize) {
            return 1.0F;
        }

        public void onAddedToWorld() {
            activeQueens.add(this);

            super.onAddedToWorld();
        }
        public void onRemovedFromWorld() {
            activeQueens.remove(this);

            this.children.stream()
                    .filter(LivingEntity::isAlive)
                    .anyMatch(candidate -> {
                        Queen heir = candidate.becomeQueen();

                        if(heir != null){
                            heir.children.addAll(this.children);
                            this.children.removeIf(child -> child == this);
                            this.children.forEach(child -> child.setParent(heir));
                            return true;
                        }

                        return false;
                    });

            super.onRemovedFromWorld();
        }

        protected void playStepSound(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
            this.playSound(SoundEvents.SPIDER_STEP, 3.0F, 0.2F);
        }
        protected SoundEvent getAmbientSound() {
            return ModSounds.HONZIADE_AMBIENT.get();
        }
        protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
            this.playSound(SoundEvents.GHAST_HURT, 0.8F, 2.0F);
            this.playSound(SoundEvents.CREEPER_HURT, 1.8F, .75F);
            this.playSound(SoundEvents.SPIDER_HURT, 2.0F, 0.8F);

            return SoundEvents.HOSTILE_HURT;
        }
        protected SoundEvent getDeathSound() {
            this.playSound(SoundEvents.GHAST_DEATH, 0.8F, 0.3F);
            this.playSound(SoundEvents.CREEPER_DEATH, 1.8F, .75F);
            this.playSound(SoundEvents.SPIDER_DEATH, 2.0F, 0.8F);

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
    }

    static class HonziadeAttackGoal extends MeleeAttackGoal {

        public HonziadeAttackGoal(HonziadeEntity pHonziade, double pSpeedModifier) {
            super(pHonziade, pSpeedModifier, true);
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget) {
            return (double)(this.mob.getBbWidth() + pAttackTarget.getBbWidth() + 7.0f);
        }
    }
}

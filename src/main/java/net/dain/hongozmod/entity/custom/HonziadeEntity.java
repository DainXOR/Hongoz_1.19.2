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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class HonziadeEntity extends Infected implements IAnimatable{
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(HonziadeEntity.class, EntityDataSerializers.BYTE);
    private static final List<Queen> ACTIVE_QUEENS = new ArrayList<>(5);
    public static final int QUEENING_AGE = 20 * 60 * 5;
    private static final int TICKS_REQUEST_ADOPT = 20 * 60 * 2;

    private static boolean crowningInProgress = false;
    private static HonziadeEntity crowned = null;

    private final List<Queen> rejects = new ArrayList<>(5);
    private int requestAdoptTimer = 0;

    private boolean isHeir = false;
    private boolean waitForHeir = false;
    private HonziadeEntity heir = null;

    private Queen parent = null;
    public int age = 0;

    public HonziadeEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 25;

        this.setParent(this.getClosestQueen(e->true).getA());
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

    @Override
    public void onRemovedFromWorld() {
        if(this.hasParent()){
            if(this.heir == null){
                this.getParent().ownChildren--;
            }
            else {
                this.getParent().adoptedChildren--;
            }
        }
        super.onRemovedFromWorld();
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
        return 1500.0d;
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

    void addHonziadeSaveData(CompoundTag pNbt) {
        pNbt.putInt("HonziadeAge", this.age);

        if(this.getParent() != null){
            pNbt.putUUID("parent", this.getParent().uuid);
        }

        pNbt.putBoolean("isHeir", this.isHeir);
        pNbt.putBoolean("waitHeir", this.waitForHeir);

        if(this.waitForHeir) {
            pNbt.putUUID("waitHeir", this.heir.uuid);
        }
    }
    void readHonziadeSaveData(CompoundTag pTag) {
        this.age = pTag.getInt("HonziadeAge");

        if(pTag.contains("parent")) {
            this.setParent(getEntityFromTag(pTag, this.level, "parent"));
        }

        this.isHeir = pTag.getBoolean("isHeir");
        this.waitForHeir = pTag.getBoolean("waitHeir");

        if(this.waitForHeir){
            this.heir = getEntityFromTag(pTag, this.level, "heir");
        }
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

            if(!this.hasParent()){
                if(this.waitForHeir){
                    if (this.heir.isQueen()){
                        Queen h = (Queen)this.heir;
                        this.waitForHeir = !h.requestAdoption(this);
                    }
                }
                else {
                    this.requestAdoptTimer++;
                    if(this.requestAdoptTimer >= TICKS_REQUEST_ADOPT){
                        this.requestAdoptTimer = 0;
                        Queen parentCandidate = this.getClosestQueen(this.rejects::contains).getA();
                        if(parentCandidate.requestAdoption(this)){
                            this.setParent(parentCandidate);
                            this.rejects.clear();
                            return;
                        }
                        this.rejects.add(parentCandidate);
                    }
                }
            }
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

    Pair<Queen, Double> getClosestQueen(Predicate<Queen> filter){
        if(ACTIVE_QUEENS.isEmpty()){
            return new Pair<>(null, 100_000.0);
        } else if (this.isQueen()) {
            return new Pair<>((Queen) this, 0.0);
        }

        double closestDistance = 100_000;
        Queen closestQueen = null;

        for (Queen queen : ACTIVE_QUEENS) {
            if(!filter.test(queen)){
                continue;
            }
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
        return (
                !this.isAggressive() &&
                this.age >= QUEENING_AGE &&
                this.getHealth() == this.getMaxHealth() &&
                this.hasParent() &&
                this.distanceToSqr(this.getParent()) > this.getParent().getAlertRange() * this.getParent().getAlertRange()
                ) || (
                this.isAggressive() &&
                this.alertedAllies() &&
                this.getAlertedAlliesAmount() == 0 &&
                this.getHealth() <= this.getMaxHealth() * 0.75f
                );

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
        healthRatio *= (float)this.age / (float) QUEENING_AGE;
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
        public static final int CHILDREN_LIMIT = 30;
        public static final int ADOPT_LIMIT = 20;

        private final List<HonziadeEntity> children = new ArrayList<>(15);
        private int ownChildren = 0;
        private int adoptedChildren = 0;

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
        public boolean requestAdoption(@NotNull HonziadeEntity entity){
            if((entity.waitForHeir && entity.heir == this) ||
               (this.random.nextFloat() > ((float) this.adoptedChildren / ADOPT_LIMIT))){

                this.adoptedChildren++;
                this.children.add(entity);
                return true;
            }

            return false;
        }

        @Override
        protected int getAlertTicks() {
            return 20 * 30;
        }
        protected final void alertOthers() {
            if (this.getTarget() == null){
                return;
            }

            this.playRoarSound();
            this.children.removeIf(Objects::isNull);
            this.children.stream()
                    .filter((child) -> child.getTarget() == null)
                    .forEach((child) -> {
                child.setTarget(this.getTarget());
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

            if(this.canSpawnReinforcements(waitProgress) && this.ownChildren < CHILDREN_LIMIT){
                int reinforcementCount = this.isAggressive()?
                        (int)(REINFORCEMENTS_AMOUNT.getMinValue() * (1 + waitProgress)) :
                        REINFORCEMENTS_AMOUNT.sample(this.random);

                return this.spawnReinforcements(reinforcementCount);
            }
            return false;
        }
        @Contract(value = "_->true")
        public boolean spawnReinforcements(int count){
            for (int i = 0; i <= count; i++){
                HonziadeEntity child = ModEntityTypes.HONZIADE.get().create(this.level);
                assert child != null;
                child.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
                child.setParent(this);
                child.setPersistenceRequired();
                this.children.add(child);

                this.level.addFreshEntity(child);
            }

            this.ownChildren += count;
            return true;
        }

        public void tick() {
            super.tick();
            REINFORCEMENTS_TICKS++;

            if(this.maybeSpawnReinforcements()){
                REINFORCEMENTS_TICKS = 0;
            }
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
            ACTIVE_QUEENS.add(this);

            super.onAddedToWorld();
        }
        public void onRemovedFromWorld() {
            ACTIVE_QUEENS.remove(this);

            this.children.removeIf(Objects::isNull);
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

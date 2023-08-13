package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class HordenEntity extends Infected {

    public static final int ALIVE_LIMIT = 10;
    public static int SPAWN_COUNT = 0;
    public static int ALIVE_COUNT = 0;

    public HordenEntity(EntityType<? extends Infected> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 35;
    }

    @Override
    public void onAddedToWorld() {
        ALIVE_COUNT += 1;
        SPAWN_COUNT += 1;
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        ALIVE_COUNT -= 1;
        super.onRemovedFromWorld();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setPersistenceRequired();

        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(pLevel.getLevel());
        assert lightningbolt != null;
        lightningbolt.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
        pLevel.addFreshEntity(lightningbolt);
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 5.0f, 1.0f);

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.00)
                .add(Attributes.ATTACK_DAMAGE, 11.00)
                .add(Attributes.ATTACK_SPEED, 0.40)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.00)
                .add(Attributes.ATTACK_KNOCKBACK, 0.50)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.80)
                .add(Attributes.ARMOR, 0.90)
                .add(Attributes.ARMOR_TOUGHNESS, 0.40)
                .build();
    }

    @Override
    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) {
        this.heal(pLightning.getDamage());
    }

    @Override
    public float customHurt(DamageSource pSource, float pAmount) {
        float newDamage = pAmount;
        float multiplier = 1 / 3f;

        if(pSource.getDirectEntity() != null){
            Entity entity = pSource.getDirectEntity();

            if (entity instanceof AbstractArrow) {
                newDamage = 1;
            }
            else if (entity instanceof LivingEntity livingEntity){
                if(livingEntity.getMainHandItem().getItem() instanceof PickaxeItem item){
                    multiplier = item.getTier().getLevel() + 1;

                    int dropCount = (multiplier >= 3 && this.random.nextFloat() >= (0.99f - ((multiplier * multiplier) / 1000))) ? 1 : 0;
                    ItemStack hitDrop = Items.NETHERITE_SCRAP.getDefaultInstance();
                    hitDrop.setCount(dropCount);
                    this.spawnAtLocation(hitDrop);
                }
                newDamage *= multiplier;
            }
        }

        return newDamage;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2d, false));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));


        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public Class<? extends Infected> getAvoidAlertType() {
        return HunterEntity.class;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(ModSounds.HORDEN_STEP.get());
        super.playStepSound(blockPos, blockState);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HORDEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.HORDEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HORDEN_DEATH.get();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
        boolean canSpawn = ALIVE_COUNT < ALIVE_LIMIT && super.checkSpawnRules(pLevel, pSpawnReason);

        return canSpawn;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }
}

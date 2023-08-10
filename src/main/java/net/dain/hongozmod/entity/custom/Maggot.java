package net.dain.hongozmod.entity.custom;

import com.mojang.math.Vector3f;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class Maggot extends Infected {
    public static final float SHADOW_RADIUS = 0.1f;

    public Maggot(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 32.0f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, AVOID_SPEED_MODIFIER));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));


        // this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Animal.class, MUST_SEE_TARGET));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, MUST_SEE_TARGET));
        this.targetSelector.addGoal(9, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // this.targetSelector.addGoal(10, new ResetUniversalAngerTargetGoal<>(this, true));

    }
    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.00)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .build();
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean result = super.doHurtTarget(pEntity);

        if(((LivingEntity)pEntity).isDeadOrDying()){
        int amount = this.random.nextIntBetweenInclusive(1, 3);
            for (int i = 0; i < amount; i++){
                Maggot parasite = ModEntityTypes.MAGGOT.get().create(this.level);
                assert parasite != null;
                parasite.moveTo(Vec3.atBottomCenterOf(pEntity.blockPosition()));
                this.level.addFreshEntity(parasite);
            }
        }
        return result;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return 0.1f;
    }

    @Override
    protected <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public String getEntityName() {
        return "fungi_maggot";
    }

    @Override
    public float getShadowRadius() {
        return 0.2f;
    }
}

package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class FungiEgg extends Infected {
    private static final int ANIMATION_DURATION = 20 * 4; // If the idle animation changes, this needs to be updated

    private int animationProgress = 0;
    public boolean isHatching = false;

    protected @NotNull EntityType<? extends Monster> innerEntity = ModEntityTypes.ZHONGO.get();

    public FungiEgg(EntityType<? extends Infected> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 1;
    }

    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.00)
                .add(Attributes.MOVEMENT_SPEED, 0.00)
                .add(Attributes.FOLLOW_RANGE, 0.00)
                .build();
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.isDeadOrDying() && !this.level.isClientSide()){
            this.animationProgress++;

            if(animationProgress == ANIMATION_DURATION){
                this.hatch();
            }
        }
        if(!this.isOnGround()){
            this.addParticlesAroundSelf(ParticleTypes.POOF, 2);
        }
        if(this.isHatching){
            this.addParticlesAroundSelf(ParticleTypes.POOF, 10);
            this.isHatching = false;
        }
    }

    protected void hatch(){
        this.isHatching = true;
        Monster inner = innerEntity.create(this.level);
        assert inner != null;

        inner.moveTo(Vec3.atCenterOf(this.blockPosition()));
        inner.setNoAi(this.isNoAi());
        if (this.hasCustomName()) {
            inner.setCustomName(this.getCustomName());
            inner.setCustomNameVisible(this.isCustomNameVisible());
        }

        this.level.addFreshEntity(inner);
        net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, inner);
        this.discard();
    }

    @Override
    protected <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        event.getController().setAnimation(getAnimation("idle", LoopType.PLAY_ONCE));
        return PlayState.CONTINUE;
    }
    @Override
    protected <E extends IAnimatable> PlayState attackAnimation(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }

    public String getEntityName(){
        return "fungi_egg";
    }
}

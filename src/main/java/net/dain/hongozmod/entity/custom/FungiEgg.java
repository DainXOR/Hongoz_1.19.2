package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.entity.templates.Infected;
import net.dain.hongozmod.entity.templates.LoopType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class FungiEgg extends Infected {

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
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected <E extends IAnimatable> PlayState normalAnimation(AnimationEvent<E> event) {
        event.getController().setAnimation(getAnimation("idle", LoopType.LOOP));
        return PlayState.CONTINUE;
    }



    public String getEntityName(){
        return "fungi_egg";
    }
}

package net.dain.hongozmod.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.UUID;

public class InfectedEntity extends Monster implements IAnimatable, NeutralMob {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public static final AnimationBuilder WALK_ANIMATION = newAnimation("", AnimationType.LOOP);

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(HunterEntity.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;

    protected InfectedEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public static String getEntityName(){
        return "infected";
    }

    public static AnimationBuilder newAnimation(String name, AnimationType loopType){
        return new AnimationBuilder().addAnimation("animation." + name + "." + getEntityName(), loopType.get());

    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {

    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {

    }

    @Override
    public void startPersistentAngerTimer() {

    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    enum AnimationType{
        LOOP(ILoopType.EDefaultLoopTypes.LOOP),
        PLAY_ONCE(ILoopType.EDefaultLoopTypes.PLAY_ONCE),
        HOLD_ON_LAST_FRAME(ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);

        private final ILoopType.EDefaultLoopTypes loopType;

        AnimationType(ILoopType.EDefaultLoopTypes loop) {
            this.loopType = loop;
        }
        AnimationType() {
            this.loopType = ILoopType.EDefaultLoopTypes.LOOP;
        }

        public ILoopType.EDefaultLoopTypes get() {
            return loopType;
        }
    }
}

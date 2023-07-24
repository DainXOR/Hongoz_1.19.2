package net.dain.hongozmod.entity.templates;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;

public class AnimationHelper {

    public static AnimationBuilder newAnimation(String entityName, String animationName, LoopType loopType){
        return new AnimationBuilder().addAnimation("animation." + entityName + "." + animationName, loopType.get());
    }
    public static AnimationController newController(IAnimatable animatable, String name, AnimationController.IAnimationPredicate predicate){
        return new AnimationController(animatable, name,0, predicate);
    }
}

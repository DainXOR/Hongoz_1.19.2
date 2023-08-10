package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class InfectedModel<E extends Infected> extends AnimatedGeoModel<E> {
    @Override
    public ResourceLocation getModelResource(E object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/" + object.getEntityName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(E object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/" + object.getEntityName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(E animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/" + animatable.getEntityName() + ".animation.json");
    }
}

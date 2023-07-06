package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HonziadeModel extends AnimatedGeoModel<HonziadeEntity> {
    @Override
    public ResourceLocation getModelResource(HonziadeEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/honziade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HonziadeEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/honziade.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HonziadeEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/honziade.animation.json");
    }
}

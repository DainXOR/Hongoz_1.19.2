package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HunterModel  extends AnimatedGeoModel<HunterEntity> {
    @Override
    public ResourceLocation getModelResource(HunterEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/hunter.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HunterEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/hunter.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HunterEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/hunter.animation.json");
    }
}

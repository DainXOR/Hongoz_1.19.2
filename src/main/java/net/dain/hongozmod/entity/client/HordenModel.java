package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.HordenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HordenModel extends AnimatedGeoModel<HordenEntity> {
    @Override
    public ResourceLocation getModelResource(HordenEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/horden.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HordenEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/horden.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HordenEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/horden.animation.json");
    }
}

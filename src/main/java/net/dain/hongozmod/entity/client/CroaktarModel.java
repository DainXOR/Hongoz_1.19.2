package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.CroaktarEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CroaktarModel extends AnimatedGeoModel<CroaktarEntity> {
    @Override
    public ResourceLocation getModelResource(CroaktarEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/croaktar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CroaktarEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/croaktar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CroaktarEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/croaktar.animation.json");
    }
}

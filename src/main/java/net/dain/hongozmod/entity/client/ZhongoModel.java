package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ZhongoModel  extends AnimatedGeoModel<ZhongoEntity> {

    @Override
    public ResourceLocation getModelResource(ZhongoEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/zhongo.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZhongoEntity object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/zhongo.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZhongoEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/zhongo.animation.json");
    }
}

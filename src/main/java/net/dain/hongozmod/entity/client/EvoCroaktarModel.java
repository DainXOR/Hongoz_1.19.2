package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.EvoCroaktar;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EvoCroaktarModel extends AnimatedGeoModel<EvoCroaktar> {
    @Override
    public ResourceLocation getModelResource(EvoCroaktar object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/evolved_croaktar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EvoCroaktar object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/evolved_croaktar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EvoCroaktar animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/evolved_croaktar.animation.json");
    }
}

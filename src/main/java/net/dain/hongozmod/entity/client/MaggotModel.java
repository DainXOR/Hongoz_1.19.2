package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.Maggot;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MaggotModel extends AnimatedGeoModel<Maggot> {
    @Override
    public ResourceLocation getModelResource(Maggot object) {
        return new ResourceLocation(HongozMod.MOD_ID, "geo/fungi_maggot.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Maggot object) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/fungi_maggot.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Maggot animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "animations/fungi_maggot.animation.json");
    }
}

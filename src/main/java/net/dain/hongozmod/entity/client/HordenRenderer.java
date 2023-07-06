package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.HordenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class HordenRenderer extends GeoEntityRenderer<HordenEntity> {

    public HordenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HordenModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(HordenEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/horden.png");
    }
}

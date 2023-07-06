package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ZhongoRenderer extends GeoEntityRenderer<ZhongoEntity> {

    public ZhongoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ZhongoModel());
        this.shadowRadius = 0.3f;
    }

    @Override
    public ResourceLocation getTextureLocation(ZhongoEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/zhongo.png");
    }
}

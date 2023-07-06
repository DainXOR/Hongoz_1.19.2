package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import net.dain.hongozmod.entity.custom.CroaktarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CroaktarRenderer extends GeoEntityRenderer<CroaktarEntity> {

    public CroaktarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CroaktarModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(CroaktarEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/croaktar.png");
    }
}

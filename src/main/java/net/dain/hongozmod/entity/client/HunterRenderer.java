package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class HunterRenderer extends GeoEntityRenderer<HunterEntity> {
    public HunterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HunterModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(HunterEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/hunter.png");
    }
}

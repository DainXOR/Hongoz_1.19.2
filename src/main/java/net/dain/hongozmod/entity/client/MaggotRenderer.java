package net.dain.hongozmod.entity.client;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.Maggot;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MaggotRenderer extends GeoEntityRenderer<Maggot> {

    public MaggotRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MaggotModel());
        this.shadowRadius = 0.1f;
    }

    @Override
    public ResourceLocation getTextureLocation(Maggot animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/fungi_maggot.png");
    }
}

package net.dain.hongozmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dain.hongozmod.HongozMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import net.dain.hongozmod.entity.custom.HonziadeEntity;

public class HonziadeRenderer extends GeoEntityRenderer<HonziadeEntity> {

    public HonziadeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HonziadeModel());
        this.shadowRadius = 1.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(HonziadeEntity animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/honziade.png");
    }

    @Override
    public RenderType getRenderType(HonziadeEntity animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        poseStack.scale(0.8f, 0.5f, 0.8f);
        return super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }
}

package net.dain.hongozmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.EvoCroaktar;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EvoCroaktarRenderer extends GeoEntityRenderer<EvoCroaktar> {

    public EvoCroaktarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EvoCroaktarModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(EvoCroaktar animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/evolved_croaktar.png");
    }

    @Override
    public RenderType getRenderType(EvoCroaktar animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        poseStack.scale(0.7f, 0.7f, 0.7f);
        return super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }
}

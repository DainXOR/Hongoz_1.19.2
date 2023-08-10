package net.dain.hongozmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.lang.reflect.Method;
import java.time.temporal.TemporalAdjusters;

public class InfectedRenderer<E extends Infected> extends GeoEntityRenderer<E> {
    protected Vector3f entityScale;

    public InfectedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new InfectedModel<E>());
    }

    @Override
    public ResourceLocation getTextureLocation(E animatable) {
        return new ResourceLocation(HongozMod.MOD_ID, "textures/entity/" + animatable.getEntityName() + ".png");
    }

    @Override
    public RenderType getRenderType(E animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        Vector3f scale = animatable.getModelScale();
        poseStack.scale(scale.x(), scale.y(), scale.z());
        this.shadowRadius = animatable.getShadowRadius();
        return super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }
}

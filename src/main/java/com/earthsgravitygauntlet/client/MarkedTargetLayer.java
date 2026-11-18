package com.earthsgravitygauntlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/** Wraps marked targets in a slightly inflated translucent light-brown earthen shell. */
public class MarkedTargetLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final LivingEntityRenderer<T, M> renderer;

    public MarkedTargetLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !ClientMarks.contains(entity.getId())) return;
        float pulse = 0.55F + 0.15F * Mth.sin(ageInTicks * 0.2F);
        int color = FastColor.ARGB32.color((int) (pulse * 255.0F), 0xB8, 0x82, 0x60);
        ResourceLocation texture = this.renderer.getTextureLocation(entity);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));

        poseStack.pushPose();
        // model space: feet sit at y = 1.5 -> inflate around the feet
        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.scale(1.07F, 1.07F, 1.07F);
        poseStack.translate(0.0F, -1.5F, 0.0F);
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
    }
}

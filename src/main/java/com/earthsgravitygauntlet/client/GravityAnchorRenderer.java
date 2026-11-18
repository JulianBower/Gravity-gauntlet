package com.earthsgravitygauntlet.client;

import com.earthsgravitygauntlet.entity.GravityAnchorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class GravityAnchorRenderer extends EntityRenderer<GravityAnchorEntity> {
    private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public GravityAnchorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public boolean shouldRender(GravityAnchorEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return true; // tethers may reach far outside the anchor's own tiny bounding box
    }

    @Override
    public void render(GravityAnchorEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        float life = entity.tickCount + partialTick;
        float fade = Mth.clamp(Math.min(life / 6.0F, (GravityAnchorEntity.LIFETIME - life) / 12.0F), 0.0F, 1.0F);
        if (fade <= 0.0F) return;

        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
        VortexRenderer.renderVortex(poseStack, buffers, life, fade);

        int caught = entity.getCaughtId();
        if (caught != -1) {
            Entity target = entity.level().getEntity(caught);
            if (target != null && target.isAlive()) {
                Vec3 from = entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
                Vec3 to = target.getPosition(partialTick).add(0.0D, target.getBbHeight() / 2.0D, 0.0D);
                VortexRenderer.renderTether(poseStack, buffers, to.subtract(from), life, fade);
            }
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(GravityAnchorEntity entity) {
        return WHITE;
    }
}

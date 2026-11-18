package com.earthsgravitygauntlet.client;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Armored dark-obsidian + bronze gauntlet (#2A1A10, #3B2618, #8C5A3C) with a glowing,
 * inward-pulsing light-brown core embedded in the back of the hand.
 * Texture rows: y=0 dark obsidian, y=21 mid brown, y=42 bronze.
 */
public class EarthGauntletModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(EarthsGravityGauntlet.id("earth_gauntlet"), "main");
    public static final ResourceLocation TEXTURE = EarthsGravityGauntlet.id("textures/entity/earth_gauntlet.png");
    public static final ResourceLocation CORE_TEXTURE = EarthsGravityGauntlet.id("textures/entity/earth_gauntlet_core.png");

    private final ModelPart root;
    private final ModelPart core;

    public EarthGauntletModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        final int dark = 0;
        final int mid = 21;
        final int bronze = 42;

        p.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(0, dark).addBox(-4.0F, 6.0F, -4.0F, 8, 4, 8), PartPose.ZERO);
        p.addOrReplaceChild("cuff_ring", CubeListBuilder.create().texOffs(0, bronze).addBox(-4.5F, 4.0F, -4.5F, 9, 2, 9), PartPose.ZERO);
        p.addOrReplaceChild("cuff_stud_l", CubeListBuilder.create().texOffs(0, bronze).addBox(-5.5F, 4.5F, -1.0F, 1, 1, 2), PartPose.ZERO);
        p.addOrReplaceChild("cuff_stud_r", CubeListBuilder.create().texOffs(0, bronze).addBox(4.5F, 4.5F, -1.0F, 1, 1, 2), PartPose.ZERO);
        p.addOrReplaceChild("palm", CubeListBuilder.create().texOffs(0, mid).addBox(-3.5F, -3.0F, -3.5F, 7, 7, 7), PartPose.ZERO);
        p.addOrReplaceChild("back_plate", CubeListBuilder.create().texOffs(0, bronze).addBox(-3.0F, -4.0F, -4.5F, 6, 7, 1), PartPose.ZERO);
        p.addOrReplaceChild("back_rim", CubeListBuilder.create().texOffs(0, dark).addBox(-3.5F, -4.5F, -4.2F, 7, 1, 1), PartPose.ZERO);
        p.addOrReplaceChild("knuckle_bar", CubeListBuilder.create().texOffs(0, dark).addBox(-3.5F, -6.0F, -3.9F, 7, 2, 2), PartPose.ZERO);

        for (int i = 0; i < 4; i++) {
            float x = -3.5F + i * 1.75F;
            p.addOrReplaceChild("finger" + i, CubeListBuilder.create().texOffs(0, mid).addBox(x, -9.0F, -3.2F, 1, 6, 2), PartPose.ZERO);
            p.addOrReplaceChild("tip" + i, CubeListBuilder.create().texOffs(0, bronze).addBox(x - 0.25F, -10.5F, -3.4F, 1.5F, 2, 2.4F), PartPose.ZERO);
        }
        p.addOrReplaceChild("thumb", CubeListBuilder.create().texOffs(0, mid).addBox(3.5F, -3.0F, -2.0F, 2, 5, 3), PartPose.ZERO);
        p.addOrReplaceChild("thumb_tip", CubeListBuilder.create().texOffs(0, bronze).addBox(3.4F, -4.5F, -2.2F, 2.2F, 1.5F, 3.4F), PartPose.ZERO);

        // glowing core embedded in the back of the hand
        p.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.75F, -1.75F, -0.8F, 3.5F, 3.5F, 1.6F),
                PartPose.offset(0.0F, -0.5F, -4.8F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, float time) {
        // body
        this.core.visible = false;
        VertexConsumer body = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.root.render(poseStack, body, light, overlay, -1);
        this.core.visible = true;

        // inward-pulsing glow: shrinks quickly towards the centre, then re-expands
        float cycle = (time / 24.0F) % 1.0F;
        float scale = 1.15F - 0.45F * cycle;
        this.core.xScale = scale;
        this.core.yScale = scale;
        this.core.zScale = 1.0F;
        int alpha = (int) (255.0F * Mth.clamp(0.55F + 0.45F * (1.0F - cycle), 0.0F, 1.0F));
        VertexConsumer glow = buffers.getBuffer(RenderType.eyes(CORE_TEXTURE));
        this.core.render(poseStack, glow, 0xF000F0, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color(alpha, 255, 255, 255));
    }
}

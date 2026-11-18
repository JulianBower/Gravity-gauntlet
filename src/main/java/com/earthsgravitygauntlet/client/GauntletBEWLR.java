package com.earthsgravitygauntlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GauntletBEWLR extends BlockEntityWithoutLevelRenderer {
    private EarthGauntletModel model;

    public GauntletBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModelSet());
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.model = null;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        if (this.model == null) {
            this.model = new EarthGauntletModel(mc.getEntityModelSet().bakeLayer(EarthGauntletModel.LAYER));
        }
        float partial = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float time = (mc.level != null ? mc.level.getGameTime() : 0L) + partial;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.scale(0.9F, -0.9F, -0.9F);
        this.model.render(poseStack, buffers, light, overlay, time);
        poseStack.popPose();
    }
}

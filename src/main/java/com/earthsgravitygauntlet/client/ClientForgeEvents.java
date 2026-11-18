package com.earthsgravitygauntlet.client;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.earthsgravitygauntlet.item.GauntletItem;
import com.earthsgravitygauntlet.logic.GravityLogic;
import com.earthsgravitygauntlet.logic.GravityVfx;
import com.earthsgravitygauntlet.network.PlantAnchorPayload;
import com.earthsgravitygauntlet.network.ToggleSelfPullModePayload;
import com.earthsgravitygauntlet.network.UpdateGauntletDistancePayload;
import com.earthsgravitygauntlet.registry.ModDataComponents;
import com.earthsgravitygauntlet.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EarthsGravityGauntlet.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    private static boolean isGauntlet(ItemStack stack) {
        return stack.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get());
    }

    /** Scroll wheel adjusts the gravity point distance instead of switching hotbar slots. */
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) return;
        ItemStack stack = player.getMainHandItem();
        if (!isGauntlet(stack)) return;
        double scroll = event.getScrollDeltaY();
        if (scroll == 0.0D) return;
        event.setCanceled(true);

        double delta = Math.signum(scroll) * 1.5D;
        double distance = Mth.clamp(GauntletItem.getDistance(stack) + delta,
                GauntletItem.MIN_DISTANCE, GauntletItem.MAX_DISTANCE);
        stack.set(ModDataComponents.CURSOR_DISTANCE.get(), distance); // immediate local feedback
        PacketDistributor.sendToServer(new UpdateGauntletDistancePayload(delta));
        player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.distance",
                String.format("%.1f", distance)), true);
    }

    /**
     * Left-click while holding right-click plants an anchor. Vanilla swallows attack clicks while an item is
     * being used, so we read the raw mouse press. A normal left-click (no right-click held) stays a melee swing.
     */
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) return;
        if (!mc.options.keyAttack.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) return;
        if (!player.isUsingItem()) return;
        if (!isGauntlet(player.getItemInHand(player.getUsedItemHand()))) return;
        PacketDistributor.sendToServer(new PlantAnchorPayload());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        while (ClientModEvents.TOGGLE_SELF_PULL.consumeClick()) {
            if (!GauntletItem.find(mc.player).isEmpty()) {
                PacketDistributor.sendToServer(new ToggleSelfPullModePayload());
            }
        }

        // inward-streaming earth particles for every live cursor point
        for (Player p : mc.level.players()) {
            if (!p.isUsingItem()) continue;
            ItemStack stack = p.getItemInHand(p.getUsedItemHand());
            if (!isGauntlet(stack)) continue;
            Vec3 point = GravityLogic.cursorPoint(mc.level, p, GauntletItem.getDistance(stack), 1.0F);
            GravityVfx.inwardDust(mc.level, point, mc.level.random, 2);
        }
    }

    /** Renders the live, unplanted gravity point along each gauntlet user's look vector. */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float time = mc.level.getGameTime() + partial;
        boolean drew = false;

        for (Player p : mc.level.players()) {
            if (!p.isUsingItem()) continue;
            ItemStack stack = p.getItemInHand(p.getUsedItemHand());
            if (!isGauntlet(stack)) continue;
            Vec3 point = GravityLogic.cursorPoint(mc.level, p, GauntletItem.getDistance(stack), partial);
            poseStack.pushPose();
            poseStack.translate(point.x - cam.x, point.y - cam.y, point.z - cam.z);
            VortexRenderer.renderVortex(poseStack, buffers, time, 1.0F);
            poseStack.popPose();
            drew = true;
        }
        if (drew) {
            buffers.endBatch(ModRenderTypes.VORTEX_SOLID);
            buffers.endBatch(ModRenderTypes.VORTEX_GLOW);
        }
    }
}

package com.earthsgravitygauntlet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Draws the compact, fluid, inward-rippling gravity point and the wavy energy tethers. */
public final class VortexRenderer {
    private static final int LAT = 12;
    private static final int LON = 20;
    private static final int SHELLS = 5;

    private VortexRenderer() {}

    /** Renders a vortex centred on the current pose origin. {@code fade} is 0..1. */
    public static void renderVortex(PoseStack poseStack, MultiBufferSource buffers, float time, float fade) {
        Matrix4f matrix = poseStack.last().pose();

        // 5 staggered wavy shells rushing inward from 1.6 -> 0.25 and fading in/out seamlessly
        VertexConsumer glow = buffers.getBuffer(ModRenderTypes.VORTEX_GLOW);
        for (int i = 0; i < SHELLS; i++) {
            float p = ((time / 50.0F) + (float) i / SHELLS) % 1.0F;
            float radius = Mth.lerp(p, 1.6F, 0.25F);
            float alpha = Mth.clamp(p / 0.12F, 0.0F, 1.0F) * Mth.clamp((1.0F - p) / 0.3F, 0.0F, 1.0F) * 0.42F * fade;
            shell(glow, matrix, radius, time, i, 0xB88260, 0xB88260, alpha, 1.0F);
        }

        // compact semi-transparent core (~0.35, ~55% alpha), gently pulsing
        VertexConsumer solid = buffers.getBuffer(ModRenderTypes.VORTEX_SOLID);
        float pulse = 1.0F + 0.1F * Mth.sin(time * 0.45F);
        shell(solid, matrix, 0.35F * pulse, time, 7, 0x8C5A3C, 0xA66E4E, 0.55F * fade, 0.45F);
    }

    /** Renders a wavy translucent tether from the current origin to {@code to}. */
    public static void renderTether(PoseStack poseStack, MultiBufferSource buffers, Vec3 to, float time, float fade) {
        double length = to.length();
        if (length < 0.05D) return;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(ModRenderTypes.VORTEX_GLOW);
        Vec3 dir = to.scale(1.0D / length);
        Vec3 perp1 = Math.abs(dir.y) > 0.95D ? new Vec3(1, 0, 0).cross(dir) : dir.cross(new Vec3(0, 1, 0));
        perp1 = perp1.normalize();
        Vec3 perp2 = dir.cross(perp1).normalize();
        int segments = Math.max(8, (int) (length * 3.0D));
        float width = 0.07F;
        Vec3 prev = null;
        for (int i = 0; i <= segments; i++) {
            float f = (float) i / segments;
            float env = Mth.sin(f * Mth.PI);
            double w1 = Math.sin(i * 0.8D - time * 0.45D) * 0.22D * env;
            double w2 = Math.cos(i * 0.65D + time * 0.38D) * 0.22D * env;
            Vec3 point = to.scale(f).add(perp1.scale(w1)).add(perp2.scale(w2));
            if (prev != null) {
                ribbon(consumer, matrix, prev, point, perp1.scale(width), fade * 0.8F);
                ribbon(consumer, matrix, prev, point, perp2.scale(width), fade * 0.8F);
            }
            prev = point;
        }
    }

    private static void ribbon(VertexConsumer c, Matrix4f m, Vec3 a, Vec3 b, Vec3 side, float alpha) {
        int al = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        c.addVertex(m, (float) (a.x - side.x), (float) (a.y - side.y), (float) (a.z - side.z)).setColor(0xB8, 0x82, 0x60, al);
        c.addVertex(m, (float) (a.x + side.x), (float) (a.y + side.y), (float) (a.z + side.z)).setColor(0xB8, 0x82, 0x60, al);
        c.addVertex(m, (float) (b.x + side.x), (float) (b.y + side.y), (float) (b.z + side.z)).setColor(0xB8, 0x82, 0x60, al);
        c.addVertex(m, (float) (b.x - side.x), (float) (b.y - side.y), (float) (b.z - side.z)).setColor(0xB8, 0x82, 0x60, al);
    }

    /** A sphere whose radius undulates with sin/cos ridges, so it reads as a wavy ripple rather than a circle. */
    private static void shell(VertexConsumer c, Matrix4f m, float radius, float time, int seed,
                              int colorTop, int colorBottom, float alpha, float waveScale) {
        int cols = LON + 1;
        float[] xs = new float[(LAT + 1) * cols];
        float[] ys = new float[xs.length];
        float[] zs = new float[xs.length];
        for (int i = 0; i <= LAT; i++) {
            float theta = -Mth.HALF_PI + Mth.PI * i / LAT;
            float cosT = Mth.cos(theta);
            float sinT = Mth.sin(theta);
            for (int j = 0; j <= LON; j++) {
                float phi = Mth.TWO_PI * j / LON;
                float wave = 0.11F * Mth.sin(phi * 3.0F + time * 0.22F + seed * 1.7F) * Mth.cos(theta * 2.0F - time * 0.17F)
                        + 0.06F * Mth.sin(theta * 5.0F + phi * 2.0F + time * 0.30F + seed);
                float r = radius * (1.0F + wave * waveScale);
                int idx = i * cols + j;
                xs[idx] = r * cosT * Mth.cos(phi);
                ys[idx] = r * sinT;
                zs[idx] = r * cosT * Mth.sin(phi);
            }
        }
        int al = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        for (int i = 0; i < LAT; i++) {
            float t0 = (float) i / LAT;
            float t1 = (float) (i + 1) / LAT;
            for (int j = 0; j < LON; j++) {
                int a = i * cols + j;
                int b = a + 1;
                int d = (i + 1) * cols + j;
                int e = d + 1;
                vertex(c, m, xs[a], ys[a], zs[a], colorBottom, colorTop, t0, al);
                vertex(c, m, xs[b], ys[b], zs[b], colorBottom, colorTop, t0, al);
                vertex(c, m, xs[e], ys[e], zs[e], colorBottom, colorTop, t1, al);
                vertex(c, m, xs[d], ys[d], zs[d], colorBottom, colorTop, t1, al);
            }
        }
    }

    private static void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z,
                               int from, int to, float t, int alpha) {
        int r = (int) Mth.lerp(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = (int) Mth.lerp(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        c.addVertex(m, x, y, z).setColor(r, g, b, alpha);
    }
}

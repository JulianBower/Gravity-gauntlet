package com.earthsgravitygauntlet.logic;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class GravityVfx {
    private static final DustParticleOptions EARTH_DUST =
            new DustParticleOptions(new Vector3f(0.72F, 0.51F, 0.38F), 1.0F);

    private GravityVfx() {}

    /** Client-side: spawns light-brown earth particles streaming inward towards {@code center}. */
    public static void inwardDust(Level level, Vec3 center, RandomSource random, int count) {
        for (int i = 0; i < count; i++) {
            Vec3 off = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
            if (off.lengthSqr() < 1.0E-4D) continue;
            off = off.normalize().scale(1.2D + random.nextDouble() * 0.6D);
            level.addParticle(EARTH_DUST,
                    center.x + off.x, center.y + off.y, center.z + off.z,
                    -off.x * 0.12D, -off.y * 0.12D, -off.z * 0.12D);
        }
    }
}

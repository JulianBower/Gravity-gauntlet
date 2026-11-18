package com.earthsgravitygauntlet.logic;

import com.earthsgravitygauntlet.entity.GravityAnchorEntity;
import com.earthsgravitygauntlet.item.GauntletItem;
import com.earthsgravitygauntlet.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Stateless gravity simulation helpers. No global mutable state: everything is derived from the level. */
public final class GravityLogic {
    public static final int MAX_ANCHORS = 2;
    public static final double LINK_RANGE = 14.0D;
    public static final float CRUSH_DAMAGE = 8.0F;
    public static final float TEAR_DAMAGE = 4.0F;
    public static final String TAG_NOFALL_UNTIL = "egg_nofall_until";
    public static final String TAG_MARK_HASH = "egg_mark_hash";

    private GravityLogic() {}

    // ------------------------------------------------------------------ cursor

    public static Vec3 cursorPoint(Level level, Player player, double distance, float partial) {
        Vec3 eye = player.getEyePosition(partial);
        Vec3 look = player.getViewVector(partial);
        Vec3 end = eye.add(look.scale(distance));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            double d = Math.max(0.6D, eye.distanceTo(hit.getLocation()) - 0.45D);
            return eye.add(look.scale(d));
        }
        return end;
    }

    /** Live, unplanted cursor point: runs while the owner holds right-click. Never touches the owner. */
    public static void tickLiveCursor(ServerPlayer player) {
        if (!player.isUsingItem()) return;
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        if (!stack.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return;
        if (GauntletItem.isSelfPull(stack)) return; // self-pull only affects planted anchors
        ServerLevel level = player.serverLevel();
        Vec3 point = cursorPoint(level, player, GauntletItem.getDistance(stack), 1.0F);
        pull(level, player, point, 9.0D, GauntletItem.getMarks(stack), player.tickCount % 8 == 0);
    }

    // ------------------------------------------------------------------ anchors

    public static List<GravityAnchorEntity> findAnchors(ServerLevel level, UUID owner) {
        return new ArrayList<>(level.getEntities(EntityTypeTest.forClass(GravityAnchorEntity.class),
                a -> a.isAlive() && owner.equals(a.getOwnerUUID())));
    }

    public static void plant(ServerPlayer player) {
        if (!player.isUsingItem()) return;
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        if (!stack.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return;
        ServerLevel level = player.serverLevel();
        Vec3 pos;
        if (player.isShiftKeyDown()) {
            pos = cursorPoint(level, player, GauntletItem.getDistance(stack), 1.0F);
        } else {
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getLookAngle().scale(24.0D));
            BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.BLOCK) {
                player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.no_surface"), true);
                return;
            }
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
            pos = hit.getLocation().add(normal.scale(0.35D));
        }

        List<GravityAnchorEntity> existing = findAnchors(level, player.getUUID());
        existing.sort(Comparator.comparingInt((GravityAnchorEntity a) -> a.tickCount).reversed()); // oldest first
        while (existing.size() >= MAX_ANCHORS) {
            existing.remove(0).discard();
        }
        level.addFreshEntity(new GravityAnchorEntity(level, player, pos));
        player.getPersistentData().putBoolean(GauntletItem.TAG_PLANTED, true);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.DEEPSLATE_PLACE, SoundSource.PLAYERS, 1.2F, 0.5F);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, 0.6F);
    }

    /** Normal (entity pull) behaviour of a planted anchor, including dual-anchor tearing. */
    public static void entityMode(ServerLevel level, Player owner, GravityAnchorEntity self, List<UUID> marks) {
        GravityAnchorEntity partner = null;
        for (GravityAnchorEntity other : findAnchors(level, owner.getUUID())) {
            if (other != self && other.getCore().distanceTo(self.getCore()) <= LINK_RANGE) {
                partner = other;
                break;
            }
        }
        if (partner != null) {
            if (self.getId() < partner.getId()) {
                rip(level, owner, self, partner, marks);
            }
            return;
        }
        self.setCaughtId(-1);
        pull(level, owner, self.getCore(), 10.0D, marks, self.tickCount % 8 == 0);
    }

    // ------------------------------------------------------------------ pulling

    private static boolean isTarget(LivingEntity target, Player owner, List<UUID> marks) {
        if (target == owner || !target.isAlive() || target.isSpectator() || target instanceof ArmorStand) return false;
        return marks.isEmpty() || marks.contains(target.getUUID());
    }

    private static DamageSource crushSource(ServerLevel level, Player owner) {
        return level.damageSources().playerAttack(owner);
    }

    /** Violently pulls targets (marked only, if any are marked) into {@code point} and crushes them there. */
    public static void pull(ServerLevel level, Player owner, Vec3 point, double radius,
                            List<UUID> marks, boolean doUproot) {
        AABB box = new AABB(point, point).inflate(radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, t -> isTarget(t, owner, marks))) {
            Vec3 toPoint = point.subtract(e.getBoundingBox().getCenter());
            double dist = toPoint.length();
            if (dist > radius) continue;
            if (dist > 0.05D) {
                double speed = Math.min(Math.min(0.25D + dist * 0.45D, 1.6D), dist * 0.9D + 0.1D);
                e.setDeltaMovement(toPoint.scale(speed / dist));
                e.hurtMarked = true;
            }
            e.fallDistance = 0.0F;
            if (dist <= 1.4D && e.invulnerableTime <= 0) {
                e.hurt(crushSource(level, owner), CRUSH_DAMAGE);
            }
        }
        if (doUproot) {
            uproot(level, point);
        }
    }

    /** Two planted anchors: opposing pulls suspend targets between them and tear them apart. */
    public static void rip(ServerLevel level, Player owner, GravityAnchorEntity a, GravityAnchorEntity b,
                           List<UUID> marks) {
        Vec3 ca = a.getCore();
        Vec3 cb = b.getCore();
        Vec3 mid = ca.add(cb).scale(0.5D);
        double reach = ca.distanceTo(cb) / 2.0D + 2.5D;
        boolean tear = a.tickCount % 10 == 0;
        LivingEntity closest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(mid, mid).inflate(reach),
                t -> isTarget(t, owner, marks))) {
            Vec3 center = e.getBoundingBox().getCenter();
            double d = center.distanceTo(mid);
            if (d > reach) continue;
            Vec3 v = mid.subtract(center).scale(0.35D);
            if (v.length() > 0.9D) v = v.normalize().scale(0.9D);
            e.setDeltaMovement(v);
            e.hurtMarked = true;
            e.fallDistance = 0.0F;
            if (tear) {
                e.invulnerableTime = 0;
                e.hurt(level.damageSources().indirectMagic(a, owner), TEAR_DAMAGE); // bypasses armor
            }
            if (d < best) {
                best = d;
                closest = e;
            }
        }
        int id = closest == null ? -1 : closest.getId();
        a.setCaughtId(id);
        b.setCaughtId(id);
        if (tear) {
            uproot(level, mid);
        }
    }

    /** Self-Pull Mode: the planted anchor nearest to the owner snaps and holds the owner at its core. */
    public static void selfPull(ServerLevel level, Player owner, GravityAnchorEntity self) {
        GravityAnchorEntity nearest = self;
        double best = owner.distanceToSqr(self);
        for (GravityAnchorEntity other : findAnchors(level, owner.getUUID())) {
            double d = owner.distanceToSqr(other);
            if (d < best - 1.0E-6D || (Math.abs(d - best) <= 1.0E-6D && other.getId() < nearest.getId())) {
                best = d;
                nearest = other;
            }
        }
        if (nearest != self) return;

        owner.getPersistentData().putLong(TAG_NOFALL_UNTIL, level.getGameTime() + 60L); // 3 s grace
        owner.fallDistance = 0.0F;
        Vec3 delta = self.getCore().subtract(owner.getBoundingBox().getCenter());
        double dist = delta.length();
        if (dist > 64.0D) return;
        Vec3 velocity = dist < 0.05D ? Vec3.ZERO : delta.scale(Math.min(dist * 0.75D, 2.0D) / dist);
        owner.setDeltaMovement(velocity);
        owner.hurtMarked = true;
    }

    // ------------------------------------------------------------------ block uprooting

    private static boolean isLoose(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.MYCELIUM);
    }

    /** Rips a couple of loose surface blocks (never bedrock/obsidian) out of the ground towards the point. */
    public static void uproot(ServerLevel level, Vec3 point) {
        RandomSource random = level.random;
        int spawned = 0;
        for (int i = 0; i < 12 && spawned < 2; i++) {
            BlockPos pos = BlockPos.containing(
                    point.x + (random.nextDouble() * 2.0D - 1.0D) * 4.0D,
                    point.y + (random.nextDouble() * 2.0D - 1.0D) * 4.0D,
                    point.z + (random.nextDouble() * 2.0D - 1.0D) * 4.0D);
            if (pos.getCenter().distanceToSqr(point) > 16.0D) continue;
            BlockState state = level.getBlockState(pos);
            if (!isLoose(state)) continue;
            BlockState above = level.getBlockState(pos.above());
            if (!above.isAir() && !above.canBeReplaced()) continue; // surface blocks only
            FallingBlockEntity block = FallingBlockEntity.fall(level, pos, state);
            Vec3 toPoint = point.subtract(block.position());
            if (toPoint.lengthSqr() > 1.0E-4D) {
                block.setDeltaMovement(toPoint.normalize().scale(0.55D));
                block.hasImpulse = true;
            }
            spawned++;
        }
    }
}

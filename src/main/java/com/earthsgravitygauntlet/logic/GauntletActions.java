package com.earthsgravitygauntlet.logic;

import com.earthsgravitygauntlet.item.GauntletItem;
import com.earthsgravitygauntlet.network.SyncMarkedPayload;
import com.earthsgravitygauntlet.registry.ModDataComponents;
import com.earthsgravitygauntlet.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-side handlers for gauntlet packets and actions. */
public final class GauntletActions {
    private GauntletActions() {}

    public static void changeDistance(ServerPlayer player, double delta) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return;
        double clampedDelta = Mth.clamp(delta, -1.5D, 1.5D);
        double distance = Mth.clamp(GauntletItem.getDistance(stack) + clampedDelta,
                GauntletItem.MIN_DISTANCE, GauntletItem.MAX_DISTANCE);
        stack.set(ModDataComponents.CURSOR_DISTANCE.get(), distance);
    }

    public static void toggleSelfPull(ServerPlayer player) {
        ItemStack stack = GauntletItem.find(player);
        if (stack.isEmpty()) return;
        boolean on = !GauntletItem.isSelfPull(stack);
        stack.set(ModDataComponents.SELF_PULL_MODE.get(), on);
        player.displayClientMessage(Component.translatable(on
                ? "message.earths_gravity_gauntlet.self_pull_on"
                : "message.earths_gravity_gauntlet.self_pull_off"), true);
        player.serverLevel().playSound(null, player.blockPosition(),
                on ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8F, on ? 0.7F : 0.9F);
    }

    /** Sneak + right-click tap: toggles the looked-at living entity in the mark list; on air clears all. */
    public static void toggleMark(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(40.0D));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        double best = blockHit.getType() == HitResult.Type.MISS ? 40.0D : eye.distanceTo(blockHit.getLocation());
        LivingEntity picked = null;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().expandTowards(look.scale(40.0D)).inflate(1.5D),
                t -> t != player && t.isAlive() && !t.isSpectator())) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.3D).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceTo(hit.get());
                if (d < best) {
                    best = d;
                    picked = e;
                }
            }
        }

        List<UUID> marks = new ArrayList<>(GauntletItem.getMarks(stack));
        if (picked == null) {
            marks.clear();
            player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.cleared"), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        } else if (marks.remove(picked.getUUID())) {
            player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.unmarked",
                    picked.getDisplayName(), marks.size(), GauntletItem.MAX_TARGETS), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        } else if (marks.size() >= GauntletItem.MAX_TARGETS) {
            player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.mark_full",
                    GauntletItem.MAX_TARGETS), true);
            return;
        } else {
            marks.add(picked.getUUID());
            player.displayClientMessage(Component.translatable("message.earths_gravity_gauntlet.marked",
                    picked.getDisplayName(), marks.size(), GauntletItem.MAX_TARGETS), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        stack.set(ModDataComponents.MARKED_TARGETS.get(), List.copyOf(marks));
        syncMarks(player, true);
    }

    /** Sends the entity ids of marked mobs to the owning client when they changed. */
    public static void syncMarks(ServerPlayer player, boolean force) {
        ItemStack stack = GauntletItem.find(player);
        List<Integer> ids = new ArrayList<>();
        if (!stack.isEmpty()) {
            for (UUID uuid : GauntletItem.getMarks(stack)) {
                Entity e = player.serverLevel().getEntity(uuid);
                if (e != null && e.isAlive()) ids.add(e.getId());
            }
        }
        var data = player.getPersistentData();
        int hash = ids.hashCode();
        if (!force) {
            if (!data.contains(GravityLogic.TAG_MARK_HASH) && ids.isEmpty()) return;
            if (data.contains(GravityLogic.TAG_MARK_HASH) && data.getInt(GravityLogic.TAG_MARK_HASH) == hash) return;
        }
        data.putInt(GravityLogic.TAG_MARK_HASH, hash);
        PacketDistributor.sendToPlayer(player, new SyncMarkedPayload(ids));
    }
}

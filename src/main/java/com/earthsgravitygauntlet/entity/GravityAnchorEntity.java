package com.earthsgravitygauntlet.entity;

import com.earthsgravitygauntlet.item.GauntletItem;
import com.earthsgravitygauntlet.logic.GravityLogic;
import com.earthsgravitygauntlet.logic.GravityVfx;
import com.earthsgravitygauntlet.registry.ModEntities;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GravityAnchorEntity extends Entity {
    public static final int LIFETIME = 160; // 8.0 seconds
    private static final EntityDataAccessor<Integer> CAUGHT_ID =
            SynchedEntityData.defineId(GravityAnchorEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUUID;

    public GravityAnchorEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Creates an anchor whose core sits exactly at {@code corePos}. */
    public GravityAnchorEntity(Level level, Player owner, Vec3 corePos) {
        this(ModEntities.GRAVITY_ANCHOR.get(), level);
        this.ownerUUID = owner.getUUID();
        this.setPos(corePos.x, corePos.y - 0.25D, corePos.z);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Vec3 getCore() {
        return this.position().add(0.0D, this.getBbHeight() / 2.0D, 0.0D);
    }

    public int getCaughtId() {
        return this.entityData.get(CAUGHT_ID);
    }

    public void setCaughtId(int id) {
        if (this.entityData.get(CAUGHT_ID) != id) {
            this.entityData.set(CAUGHT_ID, id);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CAUGHT_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            GravityVfx.inwardDust(this.level(), this.getCore(), this.random, 2);
            return;
        }
        if (this.tickCount >= LIFETIME || this.ownerUUID == null) {
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        Player owner = serverLevel.getPlayerByUUID(this.ownerUUID);
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        ItemStack gauntlet = GauntletItem.find(owner);
        if (!gauntlet.isEmpty() && GauntletItem.isSelfPull(gauntlet)) {
            this.setCaughtId(-1);
            GravityLogic.selfPull(serverLevel, owner, this);
            return;
        }
        List<UUID> marks = gauntlet.isEmpty() ? List.of() : GauntletItem.getMarks(gauntlet);
        GravityLogic.entityMode(serverLevel, owner, this, marks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }
}

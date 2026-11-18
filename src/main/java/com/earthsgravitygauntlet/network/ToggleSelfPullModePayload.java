package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the G key was pressed. */
public record ToggleSelfPullModePayload() implements CustomPacketPayload {
    public static final Type<ToggleSelfPullModePayload> TYPE =
            new Type<>(EarthsGravityGauntlet.id("toggle_self_pull_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleSelfPullModePayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleSelfPullModePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

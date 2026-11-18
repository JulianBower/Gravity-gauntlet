package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: left-click while holding right-click. Sneak state is read server-side. */
public record PlantAnchorPayload() implements CustomPacketPayload {
    public static final Type<PlantAnchorPayload> TYPE = new Type<>(EarthsGravityGauntlet.id("plant_anchor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlantAnchorPayload> STREAM_CODEC =
            StreamCodec.unit(new PlantAnchorPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

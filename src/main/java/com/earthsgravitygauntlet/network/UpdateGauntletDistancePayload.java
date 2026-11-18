package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: scroll wheel changed the cursor distance by {@code delta} blocks (+-1.5 per notch). */
public record UpdateGauntletDistancePayload(double delta) implements CustomPacketPayload {
    public static final Type<UpdateGauntletDistancePayload> TYPE =
            new Type<>(EarthsGravityGauntlet.id("update_gauntlet_distance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateGauntletDistancePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.DOUBLE, UpdateGauntletDistancePayload::delta,
                    UpdateGauntletDistancePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: entity ids of currently marked targets (used by the earthen shell layer). */
public record SyncMarkedPayload(List<Integer> ids) implements CustomPacketPayload {
    public static final Type<SyncMarkedPayload> TYPE = new Type<>(EarthsGravityGauntlet.id("sync_marked"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMarkedPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).map(SyncMarkedPayload::new, SyncMarkedPayload::ids)
                    .<RegistryFriendlyByteBuf>mapStream(buf -> buf);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

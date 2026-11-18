package com.earthsgravitygauntlet.registry;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EarthsGravityGauntlet.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> CURSOR_DISTANCE =
            COMPONENTS.register("cursor_distance", () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SELF_PULL_MODE =
            COMPONENTS.register("self_pull_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> MARKED_TARGETS =
            COMPONENTS.register("marked_targets", () -> DataComponentType.<List<UUID>>builder()
                    .persistent(UUIDUtil.CODEC.listOf())
                    .networkSynchronized(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    private ModDataComponents() {}
}

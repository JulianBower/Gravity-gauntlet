package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.earthsgravitygauntlet.client.ClientMarks;
import com.earthsgravitygauntlet.logic.GauntletActions;
import com.earthsgravitygauntlet.logic.GravityLogic;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = EarthsGravityGauntlet.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(UpdateGauntletDistancePayload.TYPE, UpdateGauntletDistancePayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GauntletActions.changeDistance(player, payload.delta());
                    }
                });

        registrar.playToServer(PlantAnchorPayload.TYPE, PlantAnchorPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GravityLogic.plant(player);
                    }
                });

        registrar.playToServer(ToggleSelfPullModePayload.TYPE, ToggleSelfPullModePayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        GauntletActions.toggleSelfPull(player);
                    }
                });

        registrar.playToClient(SyncMarkedPayload.TYPE, SyncMarkedPayload.STREAM_CODEC,
                (payload, context) -> ClientMarks.set(payload.ids()));
    }
}

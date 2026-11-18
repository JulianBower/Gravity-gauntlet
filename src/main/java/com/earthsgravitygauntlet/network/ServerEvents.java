package com.earthsgravitygauntlet.network;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.earthsgravitygauntlet.logic.GauntletActions;
import com.earthsgravitygauntlet.logic.GravityLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EarthsGravityGauntlet.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GravityLogic.tickLiveCursor(player);
        if (player.tickCount % 10 == 0) {
            GauntletActions.syncMarks(player, false);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getPersistentData().remove(GravityLogic.TAG_MARK_HASH);
    }

    /** Full fall-damage negation while self-pulling and for 3 seconds afterwards. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            long until = player.getPersistentData().getLong(GravityLogic.TAG_NOFALL_UNTIL);
            if (player.level().getGameTime() <= until) {
                event.setDamageMultiplier(0.0F);
                event.setCanceled(true);
            }
        }
    }
}

package com.earthsgravitygauntlet.item;

import com.earthsgravitygauntlet.logic.GauntletActions;
import com.earthsgravitygauntlet.registry.ModDataComponents;
import com.earthsgravitygauntlet.registry.ModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class GauntletItem extends Item {
    public static final double MIN_DISTANCE = 2.0D;
    public static final double MAX_DISTANCE = 24.0D;
    public static final double DEFAULT_DISTANCE = 8.0D;
    public static final int MAX_TARGETS = 8;
    public static final int TAP_TICKS = 8;

    public static final String TAG_PLANTED = "egg_planted_this_use";
    public static final String TAG_SNEAK_START = "egg_sneak_start";

    public GauntletItem(Properties properties) {
        super(properties);
    }

    public static double getDistance(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(ModDataComponents.CURSOR_DISTANCE.get(), DEFAULT_DISTANCE),
                MIN_DISTANCE, MAX_DISTANCE);
    }

    public static boolean isSelfPull(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SELF_PULL_MODE.get(), Boolean.FALSE);
    }

    public static List<UUID> getMarks(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MARKED_TARGETS.get(), List.of());
    }

    /** Finds the gauntlet: main hand first, then off hand, then the rest of the inventory. */
    public static ItemStack find(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return main;
        ItemStack off = player.getOffhandItem();
        if (off.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return off;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ModItems.EARTHS_GRAVITY_GAUNTLET.get())) return s;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag data = player.getPersistentData();
            data.putBoolean(TAG_PLANTED, false);
            data.putBoolean(TAG_SNEAK_START, player.isShiftKeyDown());
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        int used = getUseDuration(stack) - timeLeft;
        if (used <= TAP_TICKS && data.getBoolean(TAG_SNEAK_START) && !data.getBoolean(TAG_PLANTED)) {
            GauntletActions.toggleMark(player, stack);
        }
        data.putBoolean(TAG_SNEAK_START, false);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable(isSelfPull(stack)
                ? "message.earths_gravity_gauntlet.self_pull_on"
                : "message.earths_gravity_gauntlet.self_pull_off").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.earths_gravity_gauntlet.distance",
                String.format("%.1f", getDistance(stack))).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.earths_gravity_gauntlet.marked",
                getMarks(stack).size(), MAX_TARGETS).withStyle(ChatFormatting.GRAY));
        for (int i = 1; i <= 4; i++) {
            tooltip.add(Component.translatable("tooltip.earths_gravity_gauntlet.controls_" + i)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

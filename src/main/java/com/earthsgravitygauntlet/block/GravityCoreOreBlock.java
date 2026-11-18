package com.earthsgravitygauntlet.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GravityCoreOreBlock extends DropExperienceBlock {
    public static final MapCodec<GravityCoreOreBlock> CODEC = simpleCodec(GravityCoreOreBlock::new);

    public GravityCoreOreBlock(Properties properties) {
        super(UniformInt.of(10, 20), properties);
    }

    @Override
    public MapCodec<? extends DropExperienceBlock> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide && player.hasCorrectToolForDrops(state)) {
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F, 0.5F);
            level.playSound(null, pos, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1.4F, 0.4F);
        }
    }
}

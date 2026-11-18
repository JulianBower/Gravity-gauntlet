package com.earthsgravitygauntlet.registry;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EarthsGravityGauntlet.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + EarthsGravityGauntlet.MODID))
                    .icon(() -> new ItemStack(ModItems.EARTHS_GRAVITY_GAUNTLET.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.GRAVITY_CORE_ORE.get());
                        output.accept(ModItems.EARTHS_GRAVITY_CORE.get());
                        output.accept(ModItems.EARTHS_GRAVITY_GAUNTLET.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}

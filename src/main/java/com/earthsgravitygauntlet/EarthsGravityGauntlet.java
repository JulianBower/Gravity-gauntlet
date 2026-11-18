package com.earthsgravitygauntlet;

import com.earthsgravitygauntlet.registry.ModBlocks;
import com.earthsgravitygauntlet.registry.ModCreativeTabs;
import com.earthsgravitygauntlet.registry.ModDataComponents;
import com.earthsgravitygauntlet.registry.ModEntities;
import com.earthsgravitygauntlet.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(EarthsGravityGauntlet.MODID)
public class EarthsGravityGauntlet {
    public static final String MODID = "earths_gravity_gauntlet";

    public EarthsGravityGauntlet(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

package com.earthsgravitygauntlet.registry;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.earthsgravitygauntlet.item.GauntletItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EarthsGravityGauntlet.MODID);

    public static final DeferredItem<BlockItem> GRAVITY_CORE_ORE = ITEMS.registerSimpleBlockItem(
            "gravity_core_ore", ModBlocks.GRAVITY_CORE_ORE, new Item.Properties().rarity(Rarity.RARE));

    public static final DeferredItem<Item> EARTHS_GRAVITY_CORE = ITEMS.registerSimpleItem(
            "earths_gravity_core", new Item.Properties().rarity(Rarity.EPIC).stacksTo(16).fireResistant());

    public static final DeferredItem<Item> EARTHS_GRAVITY_GAUNTLET = ITEMS.register("earths_gravity_gauntlet",
            () -> new GauntletItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(false))));

    private ModItems() {}
}

package com.earthsgravitygauntlet.registry;

import com.earthsgravitygauntlet.EarthsGravityGauntlet;
import com.earthsgravitygauntlet.entity.GravityAnchorEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EarthsGravityGauntlet.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GravityAnchorEntity>> GRAVITY_ANCHOR =
            ENTITIES.register("gravity_anchor", () -> EntityType.Builder
                    .<GravityAnchorEntity>of(GravityAnchorEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .fireImmune()
                    .noSave()
                    .build("gravity_anchor"));

    private ModEntities() {}
}

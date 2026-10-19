package com.silahmod.vehicle;

import com.silahmod.SilahMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class Cars {
    private Cars() {}
    public static final EntityType<CarEntity> CAR = Registry.register(BuiltInRegistries.ENTITY_TYPE,
        SilahMod.id("car"), FabricEntityTypeBuilder.<CarEntity>create(MobCategory.MISC, CarEntity::new).dimensions(net.minecraft.world.entity.EntityDimensions.scalable(1.8f, 1.1f)).trackRangeBlocks(64).trackedUpdateRate(1).build(ResourceKey.create(Registries.ENTITY_TYPE, SilahMod.id("car"))));
    public static void init() {}
}

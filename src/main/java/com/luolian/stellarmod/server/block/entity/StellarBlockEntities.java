package com.luolian.stellarmod.server.block.entity;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.block.StellarBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StellarBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, StellarMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CraftingAreaBlockEntity>> CRAFTING_AREA_BE =
            BLOCK_ENTITIES.register("crafting_area_be", () ->
                    BlockEntityType.Builder.of(CraftingAreaBlockEntity::new,
                            StellarBlocks.CRAFTING_AREA_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
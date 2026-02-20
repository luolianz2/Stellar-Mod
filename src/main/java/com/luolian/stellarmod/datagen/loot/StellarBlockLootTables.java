package com.luolian.stellarmod.datagen.loot;


import com.luolian.stellarmod.block.StellarBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

//用于创建物品掉落json文件
public class StellarBlockLootTables extends BlockLootSubProvider{
    public StellarBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() { //添加挖掘后掉落自身的物品（不需要掉落的需要在注册的时候写.noOcclusion())
        this.dropSelf(StellarBlocks.RAINBOW_BLOCK.get());
        this.dropSelf(StellarBlocks.COIL_BLOCK.get());
        this.dropSelf(StellarBlocks.DIMENSION_BLOCK.get());
        this.dropSelf(StellarBlocks.SPACE_STATION_BLOCK.get());
        this.dropSelf(StellarBlocks.SPACE_STATION_GLASS_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return StellarBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}

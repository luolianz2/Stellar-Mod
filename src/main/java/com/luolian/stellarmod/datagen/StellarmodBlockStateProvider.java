package com.luolian.stellarmod.datagen;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.block.StellarBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StellarmodBlockStateProvider extends BlockStateProvider {
    public StellarmodBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, StellarMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(StellarBlocks.Space_Station_Block.get(),cubeAll(StellarBlocks.Space_Station_Block.get()));
    }
}

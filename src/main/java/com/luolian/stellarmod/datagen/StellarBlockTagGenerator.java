package com.luolian.stellarmod.datagen;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.block.StellarBlocks;
import com.luolian.stellarmod.util.StellarTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

//用于生成方块标签
public class StellarBlockTagGenerator extends BlockTagsProvider {
    public StellarBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, StellarMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(StellarTags.Blocks.METAL_DETECTOR_VALUABLES)   //生成tags文件夹下的自定义tag文件
                .add(Blocks.IRON_ORE).add(Blocks.GOLD_ORE).add(Blocks.DIAMOND_ORE);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)   //添加需用稿子挖掘的方块
                .add(StellarBlocks.COIL_BLOCK.get(),
                    StellarBlocks.DIMENSION_BLOCK.get(),
                    StellarBlocks.SPACE_STATION_BLOCK.get(),
                    StellarBlocks.SPACE_STATION_GLASS_BLOCK.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL)    //添加挖掘等级为石稿的方块
                .add(StellarBlocks.COIL_BLOCK.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL)     //添加挖掘等级为铁镐的方块
                .add(StellarBlocks.DIMENSION_BLOCK.get(),
                    StellarBlocks.SPACE_STATION_BLOCK.get(),
                    StellarBlocks.SPACE_STATION_GLASS_BLOCK.get());
    }
}

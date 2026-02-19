package com.luolian.stellarmod.datagen;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.block.StellarBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

//用于生成方块状态+方块json文件
public class StellarBlockStateProvider extends BlockStateProvider {
    public StellarBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, StellarMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {  //添加方块
        blockWithItem(StellarBlocks.RAINBOW_BLOCK);
        blockWithItem(StellarBlocks.DIMENSION_BLOCK);
        blockWithItem(StellarBlocks.SPACE_STATION_BLOCK);

        pillarBlockWithItem(StellarBlocks.COIL_BLOCK);

        glassBlockWithItem(StellarBlocks.SPACE_STATION_GLASS_BLOCK);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) { //每面相同的方块
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void pillarBlockWithItem(RegistryObject<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        if (!(block instanceof RotatedPillarBlock)) {
            throw new IllegalArgumentException("pillarBlockWithItem 只能用于 RotatedPillarBlock，但传入的是: " + block.getClass().getSimpleName());
        }
        RotatedPillarBlock pillarBlock = (RotatedPillarBlock) block;

        // 获取方块注册名（例如 "coil_block"）
        String blockName = blockRegistryObject.getId().getPath();

        // 手动构建纹理路径
        ResourceLocation sideTexture = modLoc("block/" + blockName + "_side");   // 对应 assets/stellarmod/textures/block/coil_block_side.png
        ResourceLocation endTexture = modLoc("block/" + blockName + "_top");     // 对应 assets/stellarmod/textures/block/coil_block_top.png

        // 创建垂直模型（模型文件名：blockName + "_side" -> coil_block_side.json）
        ModelFile verticalModel = models().cubeColumn(blockName + "_side", sideTexture, endTexture);
        // 创建水平模型（模型文件名：blockName + "_side_horizontal" -> coil_block_side_horizontal.json）
        ModelFile horizontalModel = models().cubeColumnHorizontal(blockName + "_side_horizontal", sideTexture, endTexture);

        // 使用两个模型文件生成方块状态
        axisBlock(pillarBlock, verticalModel, horizontalModel);

        // 生成物品模型（使用垂直模型）
        simpleBlockItem(pillarBlock, verticalModel);
    }

    private void glassBlockWithItem(RegistryObject<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        if (!(block instanceof GlassBlock)) {
            throw new IllegalArgumentException("glassBlockWithItem 只能用于 GlassBlock，但传入的是: " + block.getClass().getSimpleName());
        }
        GlassBlock glassBlock = (GlassBlock) block;
        String blockName = blockRegistryObject.getId().getPath();

        // 创建玻璃模型：父模型为 block/cube_all，纹理为 stellarmod:block/<blockName>，渲染类型为 cutout
        ModelFile glassModel = models().cubeAll(blockName, blockTexture(glassBlock))
                .renderType("cutout");

        // 生成方块状态和物品模型
        simpleBlockWithItem(glassBlock, glassModel);
    }
}

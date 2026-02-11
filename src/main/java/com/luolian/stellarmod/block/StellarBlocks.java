package com.luolian.stellarmod.block;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.item.StellarItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class StellarBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, StellarMod.MOD_ID);

    // 注册方块，可以在.requiresCorrectToolForDrops()方法黑马加.noLootTable()方法来取消数据生成处对战利品表的检测
    // JAVA规范final字段名使用全大写字母和下划线分隔单词，建议养成好习惯
    public static final RegistryObject<Block> RAINBOW_BLOCK =
            registerBlock("rainbow_block", () -> new Block(BlockBehaviour.Properties.of().strength(0.5F,3.0F)));
    public static final RegistryObject<Block> COIL_BLOCK =
            registerBlock("coil_block", () -> new Block(BlockBehaviour.Properties.of().strength(3.0F,3.0F)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DIMENSION_BLOCK =
            registerBlock("dimension_block", () -> new Block(BlockBehaviour.Properties.of().strength(6.0F,3.0F)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SPACE_STATION_BLOCK =
            registerBlock("space_station_block", () -> new Block(BlockBehaviour.Properties.of().strength(4.5F,3.0F)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SPACE_STATION_GLASS_BLOCK =
            registerBlock("space_station_glass_block", () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion()));

    private static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block) {
        StellarItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {        //注册方块
        RegistryObject<T> blocks = BLOCKS.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

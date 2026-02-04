package com.luolian.stellarmod.block;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.item.StellarItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class StellarBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, StellarMod.MODID);

    //注册方块，可以在.requiresCorrectToolForDrops()方法黑马加.noLootTable()方法来取消数据生成处对战利品表的检测
    public static final RegistryObject<Block> Rainbow_Block =
            registerBlock("rainbow_block", () -> new Block(BlockBehaviour.Properties.of().strength(0.5F,3.0F)));
    public static final RegistryObject<Block> Coil_Block =
            registerBlock("coil_block", () -> new Block(BlockBehaviour.Properties.of().strength(3.0F,3.0F)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> Dimension_Block =
            registerBlock("dimension_block", () -> new Block(BlockBehaviour.Properties.of().strength(6.0F,3.0F)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> Space_Station_Block =
            registerBlock("space_station_block", () -> new Block(BlockBehaviour.Properties.of().strength(4.5F,3.0F)
                    .requiresCorrectToolForDrops()));

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

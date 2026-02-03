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

    public static final RegistryObject<Block> Rainbow_Block =
            registerBlock("rainbow_block", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F,3.0F)));
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
        StellarItems.register(eventBus);
    }
}

//package com.luolian.stellarmod.item;
//
//import com.luolian.stellarmod.StellarMod;
//import com.mojang.logging.LogUtils;
//import net.minecraft.world.item.BlockItem;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.SoundType;
//import net.minecraft.world.level.block.state.BlockBehaviour;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//import org.slf4j.Logger;
//
//public class ModItems
//{
//    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StellarMod.MODID);
//    public static final RegistryObject<Item> testblockitem = ITEMS.register("test",() -> new Item(new Item.Properties()));
//    public static void register(IEventBus eventBus)  {
//        ITEMS.register(eventBus);
//    }
//}

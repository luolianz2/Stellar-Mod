package com.luolian.stellarmod.server.item;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.item.custom.MetalDetectorItem;
import com.luolian.stellarmod.server.item.custom.MultiToolItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 添加的物品
public class StellarItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StellarMod.MOD_ID);
    public static final RegistryObject<Item> BLUSH_FACE = ITEMS.register("blush_face",() -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> METAL_DETECTOR = ITEMS.register("metal_detector",() -> new MetalDetectorItem(new Item
            .Properties().durability(100)));
    public static final RegistryObject<Item> SAPPHIRE_CRYSTAL = ITEMS.register("sapphire_crystal",() -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MULTI_FUNCTION_TOOL = ITEMS.register("multi_function_tool",() -> new MultiToolItem(new Item
            .Properties()));
    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}

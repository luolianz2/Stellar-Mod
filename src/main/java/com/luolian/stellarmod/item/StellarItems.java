package com.luolian.stellarmod.item;

import com.luolian.stellarmod.StellarMod;
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
    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}

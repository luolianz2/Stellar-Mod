//添加的物品
package com.luolian.stellarmod.item;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StellarItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StellarMod.MODID);
    public static final RegistryObject<Item> testblockitem = ITEMS.register("test",() -> new Item(new Item.Properties()));
    public static void register(IEventBus eventBus)  {
        ITEMS.register(eventBus);
    }
}

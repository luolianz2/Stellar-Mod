//模组主类
package com.luolian.stellarmod;

import com.luolian.stellarmod.block.StellarBlocks;
import com.luolian.stellarmod.item.StellarCreativeModeTabs;
import com.luolian.stellarmod.item.StellarItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StellarMod.MODID)
public class StellarMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "stellarmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public StellarMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        StellarItems.register(modEventBus);                 //调用自定义的物品注册类，将物品注册逻辑绑定到模组事件总线
        StellarCreativeModeTabs.register(modEventBus);      //调用自定义的创造模式标签注册类，将标签注册逻辑绑定到模组事件总线
        StellarBlocks.register(modEventBus);                //调用自定义的方块注册类，将方块注册逻辑绑定到模组事件总线
    }
}

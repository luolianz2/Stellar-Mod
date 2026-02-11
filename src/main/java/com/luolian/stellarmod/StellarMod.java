//模组主类
package com.luolian.stellarmod;

import com.luolian.stellarmod.block.StellarBlocks;
import com.luolian.stellarmod.item.StellarCreativeModeTabs;
import com.luolian.stellarmod.item.StellarItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StellarMod.MOD_ID)
public class StellarMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "stellarmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * @param path 命名空间路径
     * @return 以stellarmod为命名空间的<a href="https://zh.minecraft.wiki/w/%E5%91%BD%E5%90%8D%E7%A9%BA%E9%97%B4ID">命名空间ID</a>对象
     */
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public StellarMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        StellarItems.register(modEventBus);                 //调用自定义的物品注册类，将物品注册逻辑绑定到模组事件总线
        StellarCreativeModeTabs.register(modEventBus);      //调用自定义的创造模式标签注册类，将标签注册逻辑绑定到模组事件总线
        StellarBlocks.register(modEventBus);                //调用自定义的方块注册类，将方块注册逻辑绑定到模组事件总线
    }
}

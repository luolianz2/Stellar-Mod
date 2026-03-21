package com.luolian.stellarmod.server.item;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.block.StellarBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// 创建创造模式物品栏新栏位
public class StellarCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StellarMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> STELLAR_TAB =
            CREATIVE_MODE_TABS.register("stellar_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(StellarBlocks.RAINBOW_BLOCK.get()))    //图标
                    .title(Component.translatable("itemGroup.stellar_tab"))     //名称（用相应语言文件实现）
                    .displayItems((parameters, output) -> {
                        output.accept(StellarItems.BLUSH_FACE.get());     //往里面添加相应物品
                        output.accept(StellarItems.METAL_DETECTOR.get());
                        output.accept(StellarItems.SAPPHIRE_CRYSTAL.get());
                        output.accept(StellarBlocks.RAINBOW_BLOCK.get());
                        output.accept(StellarBlocks.COIL_BLOCK.get());
                        output.accept(StellarBlocks.DIMENSION_BLOCK.get());
                        output.accept(StellarBlocks.SPACE_STATION_BLOCK.get());
                        output.accept(StellarBlocks.SPACE_STATION_GLASS_BLOCK.get());
                        output.accept(StellarBlocks.SAPPHIRE_CRYSTAL_ORE.get());
                        output.accept(StellarBlocks.SAPPHIRE_CRYSTAL_BLOCK.get());
                    }).build());
    public static void register(IEventBus eventBus) {       //让封装了自定义创造物品栏的DeferredRegister注册器，绑定到事件总线
            CREATIVE_MODE_TABS.register(eventBus);
    }
}

//创建创造模式物品栏新栏位
package com.luolian.stellarmod.item;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.block.StellarBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class StellarCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StellarMod.MODID);
    public static final RegistryObject<CreativeModeTab> STELLAR_TAB =
            CREATIVE_MODE_TABS.register("stellar_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(StellarBlocks.Rainbow_Block.get()))    //图标
                    .title(Component.translatable("itemGroup.stellar_tab"))     //名称（用相应语言文件实现）
                    .displayItems((p_270258_, p_259752_) -> {
                        p_259752_.accept(StellarItems.Blush_Face.get());     //往里面添加相应物品
                        p_259752_.accept(StellarBlocks.Rainbow_Block.get());
                        p_259752_.accept(StellarBlocks.Coil_Block.get());
                        p_259752_.accept(StellarBlocks.Dimension_Block.get());
                        p_259752_.accept(StellarBlocks.Space_Station_Block.get());
                    }).build());
    public static void register(IEventBus eventBus) {       //让封装了自定义创造物品栏的DeferredRegister注册器，绑定到事件总线
            CREATIVE_MODE_TABS.register(eventBus);
    }
}

//模组主类
package com.luolian.stellarmod;

import com.luolian.stellarmod.api.compat.StellarTaskTypes;
import com.luolian.stellarmod.client.screen.craftingArea.CraftingAreaBlockScreen;
import com.luolian.stellarmod.client.screen.StellarMenuTypes;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.server.block.StellarBlocks;
import com.luolian.stellarmod.server.block.entity.StellarBlockEntities;
import com.luolian.stellarmod.server.data.modifier.StellarModifierRegistry;
import com.luolian.stellarmod.server.data.toolCore.MaterialDataLoader;
import com.luolian.stellarmod.server.effect.StellarMobEffects;
import com.luolian.stellarmod.server.item.StellarCreativeModeTabs;
import com.luolian.stellarmod.server.item.StellarItems;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.luolian.stellarmod.server.potion.StellarPotions;
import com.luolian.stellarmod.server.worldgen.dimension.EmptyChunkGenerator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StellarMod.MOD_ID)
public class StellarMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "stellarmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // 注册自定义区块生成器的 Codec（必须）
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);

    public static final RegistryObject<Codec<EmptyChunkGenerator>> VOID_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("empty", () -> EmptyChunkGenerator.CODEC);

    /**
     * @param path 命名空间路径
     * @return 以stellarmod为命名空间的<a href="https://zh.minecraft.wiki/w/%E5%91%BD%E5%90%8D%E7%A9%BA%E9%97%B4ID">命名空间ID</a>对象
     */
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ResourceLocation textureLocation(String path) {
        return location("textures/" + path);
    }

    public StellarMod(FMLJavaModLoadingContext context) {
        StellarTaskTypes.init();

        IEventBus modEventBus = context.getModEventBus();
        StellarItems.register(modEventBus);                 //调用自定义的物品注册类，将物品注册逻辑绑定到模组事件总线
        StellarCreativeModeTabs.register(modEventBus);      //调用自定义的创造模式标签注册类，将标签注册逻辑绑定到模组事件总线
        StellarBlocks.register(modEventBus);                //调用自定义的方块注册类，将方块注册逻辑绑定到模组事件总线
        StellarMobEffects.register(modEventBus);
        StellarPotions.register(modEventBus);
        StellarBlockEntities.register(modEventBus);
        StellarMenuTypes.register(modEventBus);
        StellarNetworkHandler.register();
        //注册所有副词条效果
        StellarModifierRegistry.registerAll();
        //注册区块生成器 Codec 到事件总线
        CHUNK_GENERATORS.register(modEventBus);

        //注册材料数据加载器到 Forge 事件总线
        //确保在资源重载时（包括首次加载和 /reload 命令）能够扫描并加载材料 JSON 文件
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    //事件处理方法
    /**
     * 在资源重载事件中添加自定义的数据加载器 {@link MaterialDataLoader}。
     * 该方法由 Forge 事件总线调用，负责将材料 JSON 文件的扫描逻辑注入资源加载流程。
     *
     * @param event 资源重载事件
     */
    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDataLoader());
        LOGGER.debug("MaterialDataLoader registered to AddReloadListenerEvent");
    }

    @Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                //为工具核心注册动态模型属性
                ItemProperties.register(
                        StellarItems.TOOL_CORE.get(),
                        ToolCoreItem.ACTIVE_TYPE_PREDICATE,
                        (stack, level, entity, seed) -> (float) ToolCoreItem.getActiveType(stack).ordinal()
                );

                MenuScreens.register(StellarMenuTypes.CRAFTING_AREA_MENU.get(), CraftingAreaBlockScreen::new);
            });
        }
    }
}
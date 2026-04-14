package com.luolian.stellarmod.server.event;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.data.itemcore.MaterialDataLoader;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 负责在服务端资源重载时注册 {@link MaterialDataLoader}。
 * <p>
 * 该类监听 {@link AddReloadListenerEvent} 事件，该事件仅在服务端触发，
 * 用于向资源管理器添加自定义数据重载监听器。
 * </p>
 */
@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MaterialReloadListenerRegistry {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDataLoader());
    }
}
package com.luolian.stellarmod.server.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import io.github.edwinmindcraft.calio.api.event.CalioDynamicRegistryEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventListener {
    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        OriginsUtil.buildOriginToLayerCache();

        ServerPlayer player = event.getPlayer();
        if (player != null) {
            OriginsUtil.handleOriginChange(player);
        }

        //OriginsUtil.handleOriginChange(event.getPlayer());
    }

    @SubscribeEvent
    public static void reloadComplete(CalioDynamicRegistryEvent.LoadComplete event) {
        OriginsUtil.buildOriginToLayerCache();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            OriginsUtil.handleOriginChange(serverPlayer);
        }
    }
}

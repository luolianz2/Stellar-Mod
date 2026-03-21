package com.luolian.stellarmod.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoginListener {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            OriginsUtil.handleOriginChange(serverPlayer);
        }
    }
}

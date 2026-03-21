package com.luolian.stellarmod.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import com.mojang.brigadier.context.CommandContext;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID)
public class StellarCommandRegister {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("stellar")
                        .then(literal("origin_change")
                                .executes(StellarCommandRegister::triggerOriginsChange)
                        )
                        .then(literal("origin_list")
                                .executes(StellarCommandRegister::checkOriginList)
                        )
                        .then(literal("origin_layer_list")
                                .executes(StellarCommandRegister::checkOriginLayerList)
                        )
        );
    }

    private static int checkOriginLayerList(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) return 0;

        context.getSource().getPlayer().sendSystemMessage(Component.literal(
                OriginsAPI.getLayersRegistry().keySet().toString()
        ));
        return 1;
    }

    private static int triggerOriginsChange(CommandContext<CommandSourceStack> context) {
        OriginsUtil.handleOriginChange(context.getSource().getPlayer());
        return 1;
    }

    private static int checkOriginList(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) return 0;

        context.getSource().getPlayer().sendSystemMessage(Component.literal(
                OriginsAPI.getOriginsRegistry().keySet().toString()
        ));
        return 1;
    }
}

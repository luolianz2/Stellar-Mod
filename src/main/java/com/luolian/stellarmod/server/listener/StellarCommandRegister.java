package com.luolian.stellarmod.server.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import com.mojang.brigadier.context.CommandContext;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
                        .then(literal("test")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    player.sendSystemMessage(Component.literal(
                                            OriginsUtil.getOriginIds(ResourceLocation.fromNamespaceAndPath("origins-classes", "class")).toString()
                                    ));
                                    return 1;
                                })
                        )
        );
    }

    private static int checkOriginLayerList(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.sendSystemMessage(Component.literal(
                OriginsAPI.getLayersRegistry().keySet().toString()
        ));
        return 1;
    }

    private static int triggerOriginsChange(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        OriginsUtil.handleOriginChange(player);
        return 1;
    }

    private static int checkOriginList(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.sendSystemMessage(Component.literal(
                OriginsAPI.getOriginsRegistry().keySet().toString()
        ));
        return 1;
    }
}
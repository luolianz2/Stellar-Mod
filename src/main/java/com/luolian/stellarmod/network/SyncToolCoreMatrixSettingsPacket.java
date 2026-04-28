package com.luolian.stellarmod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncToolCoreMatrixSettingsPacket {
    private final int slot;
    private final CompoundTag settings;

    public SyncToolCoreMatrixSettingsPacket(int slot, CompoundTag settings) {
        this.slot = slot;
        this.settings = settings;
    }

    public static void encode(SyncToolCoreMatrixSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
        buf.writeNbt(msg.settings);
    }

    public static SyncToolCoreMatrixSettingsPacket decode(FriendlyByteBuf buf) {
        return new SyncToolCoreMatrixSettingsPacket(buf.readVarInt(), buf.readNbt());
    }

    public static void handle(SyncToolCoreMatrixSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getInventory().getItem(msg.slot);
                if (!stack.isEmpty()) {
                    stack.getOrCreateTag().put("ToolCoreMatrixSettings", msg.settings);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
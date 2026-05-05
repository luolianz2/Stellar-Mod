package com.luolian.stellarmod.network;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncToolCoreMatrixSettingsPacket {

    private final int slot;
    private final CompoundTag settings;        //矩阵开关状态 (ToolCoreMatrixSettings)
    private final CompoundTag activeLevels;    //矩阵生效等级 (ToolCoreMatrixActiveLevels)

    /**
     * @param slot         物品在玩家背包中的槽位
     * @param settings     矩阵开关状态容器
     * @param activeLevels 矩阵生效等级容器
     */
    public SyncToolCoreMatrixSettingsPacket(int slot, CompoundTag settings, CompoundTag activeLevels) {
        this.slot = slot;
        this.settings = settings;
        this.activeLevels = activeLevels;
    }

    public static void encode(SyncToolCoreMatrixSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
        buf.writeNbt(msg.settings);
        buf.writeNbt(msg.activeLevels);
    }

    public static SyncToolCoreMatrixSettingsPacket decode(FriendlyByteBuf buf) {
        return new SyncToolCoreMatrixSettingsPacket(buf.readVarInt(), buf.readNbt(), buf.readNbt());
    }

    public static void handle(SyncToolCoreMatrixSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getInventory().getItem(msg.slot);
                if (!stack.isEmpty()) {
                    //写入矩阵开关状态
                    if (msg.settings != null && !msg.settings.isEmpty()) {
                        stack.getOrCreateTag().put(ToolCoreItem.TAG_MATRIX_SETTINGS, msg.settings);
                    } else {
                        stack.getOrCreateTag().remove(ToolCoreItem.TAG_MATRIX_SETTINGS);
                    }

                    //写入矩阵生效等级
                    if (msg.activeLevels != null && !msg.activeLevels.isEmpty()) {
                        stack.getOrCreateTag().put(ToolCoreItem.TAG_MATRIX_ACTIVE_LEVELS, msg.activeLevels);
                    } else {
                        stack.getOrCreateTag().remove(ToolCoreItem.TAG_MATRIX_ACTIVE_LEVELS);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
package com.luolian.stellarmod.network;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncToolCoreModifierSettingsPacket {

    private final int slot;
    private final CompoundTag settings;        //副词条开关状态
    private final CompoundTag activeLevels;    //副词条生效等级

    /**
     * @param slot        物品在玩家背包中的槽位
     * @param settings    副词条开关状态容器（ToolCoreModifierSettings）
     * @param activeLevels 副词条生效等级容器（ToolCoreModifierActiveLevels）
     */
    public SyncToolCoreModifierSettingsPacket(int slot, CompoundTag settings, CompoundTag activeLevels) {
        this.slot = slot;
        this.settings = settings;
        this.activeLevels = activeLevels;
    }

    public static void encode(SyncToolCoreModifierSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeNbt(msg.settings);
        //写入生效等级数据
        buf.writeNbt(msg.activeLevels);
    }

    public static SyncToolCoreModifierSettingsPacket decode(FriendlyByteBuf buf) {
        int slot = buf.readInt();
        CompoundTag settings = buf.readNbt();
        CompoundTag activeLevels = buf.readNbt(); //读取生效等级数据
        return new SyncToolCoreModifierSettingsPacket(slot, settings, activeLevels);
    }

    public static void handle(SyncToolCoreModifierSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getInventory().getItem(msg.slot);
                if (!stack.isEmpty()) {
                    //将客户端发送的副词条开关设置数据写入服务端对应的物品 NBT 中
                    //.getOrCreateTag()	获取该物品的根 NBT 复合标签（CompoundTag）。如果物品当前没有 NBT 数据，则创建一个新的空标签
                    //.put("ToolCoreModifierSettings", msg.settings)
                    //将一个键值对存入根标签。键为 "ToolCoreModifierSettings"，值为从客户端发来的 msg.settings（包含了各个副词条 ID 及其开关状态的布尔值）
                    stack.getOrCreateTag().put("ToolCoreModifierSettings", msg.settings);

                    //处理生效等级数据
                    if (msg.activeLevels != null && !msg.activeLevels.isEmpty()) {
                        stack.getOrCreateTag().put(ToolCoreItem.TAG_MODIFIER_ACTIVE_LEVELS, msg.activeLevels);
                    } else {
                        //如果为空则移除该标签，表示无限制（使用最大等级）
                        stack.getOrCreateTag().remove(ToolCoreItem.TAG_MODIFIER_ACTIVE_LEVELS);
                    }
                }
            }
        });
        //标记该网络包已被成功处理
        ctx.get().setPacketHandled(true);
    }
}
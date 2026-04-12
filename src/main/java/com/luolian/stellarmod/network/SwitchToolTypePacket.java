package com.luolian.stellarmod.network;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchToolTypePacket {

    private final ToolCoreItem.ToolType targetType;

    /**
     * 构造一个切换工具形态的包
     * @param targetType 目标形态
     */
    public SwitchToolTypePacket(ToolCoreItem.ToolType targetType) {
        this.targetType = targetType;
    }

    /**
     * 编码：将数据写入字节缓冲区
     */
    public static void encode(SwitchToolTypePacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.targetType);
    }

    /**
     * 解码：从字节缓冲区重建包对象
     */
    public static SwitchToolTypePacket decode(FriendlyByteBuf buf) {
        return new SwitchToolTypePacket(buf.readEnum(ToolCoreItem.ToolType.class));
    }

    /**
     * 处理：在服务端主线程执行形态切换
     */
    public static void handle(SwitchToolTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ToolCoreItem) {
                // 直接设置激活形态
                ToolCoreItem.setActiveType(held, msg.targetType);

                // 可选：给予玩家反馈（音效或动作栏消息）
                player.playNotifySound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.2F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
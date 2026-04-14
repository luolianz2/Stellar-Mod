package com.luolian.stellarmod.network;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//将客户端选择的工具形态发送至服务端，以更新 ToolCoreItem 的 NBT
public class SwitchToolTypePacket {

    private final ToolCoreItem.ToolType targetType;

    /**
     * 构造一个切换工具形态的包
     * @param targetType 目标形态
     */
    public SwitchToolTypePacket(ToolCoreItem.ToolType targetType) {
        this.targetType = targetType;
    }

    //编码：将数据写入字节缓冲区，当客户端要发送包时，Forge 会调用此方法，将包内的字段按照顺序写入
    public static void encode(SwitchToolTypePacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.targetType);
    }

    //解码：从字节缓冲区重建包对象，当服务端收到字节流时，Forge 会调用此方法，按相同顺序读取字段并重新构建包对象
    public static SwitchToolTypePacket decode(FriendlyByteBuf buf) {
        return new SwitchToolTypePacket(buf.readEnum(ToolCoreItem.ToolType.class));
    }

    //处理：在服务端主线程执行形态切换
    public static void handle(SwitchToolTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ToolCoreItem) {
                //直接设置激活形态
                ToolCoreItem.setActiveType(held, msg.targetType);

                //给予玩家反馈（获得经验球音效）
                player.playNotifySound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.2F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
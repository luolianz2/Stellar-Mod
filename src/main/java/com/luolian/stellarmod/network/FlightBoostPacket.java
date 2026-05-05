package com.luolian.stellarmod.network;

import com.luolian.stellarmod.common.matrix.CreativeFlightEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：同步玩家飞行加速档位。
 * <p>
 * 当玩家在客户端按下飞行加速切换键后，此包将计算出的新档位发送至服务端。
 * 服务端验证并存储后，下一个 tick 的 {@link CreativeFlightEffect#onPlayerTick} 将应用对应倍率。
 */
public class FlightBoostPacket {

    /** 目标加速档位 (0~3) */
    private final int boostTier;

    /**
     * @param boostTier 客户端计算的目标档位 (0~3)
     */
    public FlightBoostPacket(int boostTier) {
        this.boostTier = boostTier;
    }

    /**
     * 编码：将档位写入字节缓冲区。
     */
    public static void encode(FlightBoostPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.boostTier);
    }

    /**
     * 解码：从字节缓冲区读取档位并重建数据包。
     */
    public static FlightBoostPacket decode(FriendlyByteBuf buf) {
        return new FlightBoostPacket(buf.readVarInt());
    }

    /**
     * 服务端处理：验证并存储玩家飞行加速档位。
     * <p>
     * 服务端会再次扫描背包验证档位上限，防止客户端篡改导致非法档位。
     */
    public static void handle(FlightBoostPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                //服务端二次验证：档位不得超过当前背包中飞行矩阵的可用上限
                int maxTier = CreativeFlightEffect.getMaxBoostTier(player);
                int clamped = Math.max(0, Math.min(maxTier, msg.boostTier));
                CreativeFlightEffect.setBoostTier(player, clamped);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

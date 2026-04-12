package com.luolian.stellarmod.network;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class StellarNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StellarMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /**
     * 在模组初始化时调用此方法注册所有包
     */
    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                SwitchToolTypePacket.class,
                SwitchToolTypePacket::encode,
                SwitchToolTypePacket::decode,
                SwitchToolTypePacket::handle
        );
        // 未来可在此继续注册其他包，例如 OpenSettingsPacket 等
    }
}
package com.luolian.stellarmod.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.luolian.stellarmod.StellarMod.location;

//创建并配置一个专用的网络频道（Channel），然后将自定义的网络包（Packet）注册到这个频道上，以便客户端和服务端能够互相发送和接收数据
public class StellarNetworkHandler {

    //网络协议版本号，当网络包的结构发生变化时，修改网络协议版本号，不同版本的客户端与服务端将无法通信，避免因数据不匹配导致的崩溃
    private static final String PROTOCOL_VERSION = "1";

    //INSTANCE,全局唯一的 SimpleChannel 实例，模组中所有网络包的发送和接收都通过它完成
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            location("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,   //用于检查客户端版本是否被服务端接受
            PROTOCOL_VERSION::equals    //用于检查服务端版本是否被客户端接受
    );

    //每个网络包在频道中必须有一个唯一的整数 ID。这里使用自增变量 packetId++，从 0 开始依次分配
    private static int packetId = 0;

    //在模组初始化时调用此方法注册所有包
    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                SwitchToolTypePacket.class,
                SwitchToolTypePacket::encode,
                SwitchToolTypePacket::decode,
                SwitchToolTypePacket::handle
        );

        //注册同步副词条设置的包
        INSTANCE.registerMessage(
                packetId++,
                SyncToolCoreModifierSettingsPacket.class,
                SyncToolCoreModifierSettingsPacket::encode,
                SyncToolCoreModifierSettingsPacket::decode,
                SyncToolCoreModifierSettingsPacket::handle
        );

        //
        INSTANCE.registerMessage(
                packetId++,
                SyncToolCoreMatrixSettingsPacket.class,
                SyncToolCoreMatrixSettingsPacket::encode,
                SyncToolCoreMatrixSettingsPacket::decode,
                SyncToolCoreMatrixSettingsPacket::handle
        );

        //后续在此继续注册
    }
}
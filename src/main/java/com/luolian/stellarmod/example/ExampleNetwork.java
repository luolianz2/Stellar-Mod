package com.luolian.stellarmod.example;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <h1>网络通信的示例类</h1>
 * 其中演示了如何创建一个简单的网络通道，用于在客户端和服务端之间进行通信。<br>
 * <br>
 * 吐槽：Forge的网络系统设计得非常复杂，理解和使用起来都比较困难，
 * 如果可以我推荐使用<a href="https://www.mcmod.cn/class/3434.html">Architectury API</a>的网络系统。<br>
 */
public class ExampleNetwork {
    private static final String PROTOCOL_VERSION = "1.0"; // 协议版本，确保客户端和服务端使用相同的协议版本，否则无法通信
    private static int packetId = 0; // 数据包ID，使用时需要自增，确保每个数据包都有一个唯一的ID
    public static final SimpleChannel EXAMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(
            StellarMod.location("example_channel"), // 通道名称，最佳实践是使用模组ID作为命名空间
            () -> PROTOCOL_VERSION, // 用于内部获取协议版本
            PROTOCOL_VERSION::equals, // 用于验证协议版本是否匹配
            PROTOCOL_VERSION::equals // 用于验证协议版本是否匹配
    );

    public static void register() {
        // 需要在模组主类种调用这个方法来注册网络通道和数据包
        registerMessage(ExamplePacket.class, ExamplePacket::encode, ExamplePacket::decode, ExamplePacket::handle);
    }

    private static <T> void registerMessage(
            Class<T> messageType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler
    ) {
        EXAMPLE_CHANNEL.registerMessage(packetId++, messageType, encoder, decoder, handler);
    }
}

/**
 * 数据包不一定需要数据，其本身的存在就是一个信息
 * @param exampleData 一个示例数据，演示如何在数据包中携带数据
 */
record ExamplePacket(int exampleData) {
    /**
     * 编码方法，用于将数据包的数据写入到网络缓冲区中，以便发送到对方
     * @param buffer 网络缓冲区，用于写入数据包的数据
     */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(exampleData);
    }

    /**
     * 解码方法，用于从网络缓冲区中读取数据包的数据，并创建一个新的数据包对象<br>
     * <br>
     * 注意：
     * <ul>
     *     <li>方法必须是静态的，因为它需要在没有数据包对象的情况下被调用来创建一个新的数据包对象。</li>
     *     <li>解码数据的顺序必须于编码数据的顺序一致。</li>
     * </ul>
     * @param buffer 网络缓冲区，用于读取数据包的数据
     * @return 一个新的数据包对象，包含从网络缓冲区中读取的数据
     */
    public static ExamplePacket decode(FriendlyByteBuf buffer) {
        return new ExamplePacket(buffer.readInt());
    }

    /**
     * 处理方法，用于在接收到数据包时执行一些操作<br>
     * <br>
     * 注意：
     * <ul>
     *     <li>方法必须是静态的，因为它需要在没有数据包对象的情况下被调用来处理接收到的数据包。</li>
     *     <li>Forge不关心数据包的传输方向，但你需要确定，
     *     比如这是一个由客户端发送到服务端的数据包，该方法内就不应该使用客户端内容。</li>
     * </ul>
     * @param packet 接收到的数据包对象
     * @param contextSupplier 一个Supplier，用于获取网络事件的上下文对象
     *                        （使用Supplier的意图是因为注册时可以直接传入方法引用）
     */
    public static void handle(ExamplePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        // ctx是context的缩写，这及其常用，包含了关于这个网络包的一些信息
        NetworkEvent.Context ctx = contextSupplier.get();
        // 网络事件的处理在网络线程中执行，你不应该在网络线程中获取或修改游戏内容，这可能会导致线程安全问题或者游戏崩溃
        // 如果你需要在主线程中执行一些操作，你需要使用ctx.enqueueWork(Runnable)方法来安排这些操作在主线程中执行
        ctx.enqueueWork(() -> {
            int exampleData = packet.exampleData();
            // do something here
        });
        ctx.setPacketHandled(true); // 标记这个数据包已经被处理了
    }
}

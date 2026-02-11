package com.luolian.stellarmod.example;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

/**
 * <h1>需要了解的概念</h1>
 * 其中演示了在模组开发过程中需要了解的一些概念，否则可能导致实现不了所需功能。
 */
public abstract class ImportantOperation {
    /**
     * <h3>服务端和客户端的概念</h3>
     * 客户端和服务端是分离的，服务端负责游戏逻辑，客户端负责渲染和用户输入。<br>
     * 在单人游戏中，程序会自动创建一个虚拟的服务端来处理游戏逻辑，这不是一个类比，而是事实，即使你可能没有注意到。<br>
     * 因此，在开发过程中，你需要区分哪些代码应该在服务端，哪些代码应该在客户端。<br>
     * <ul>
     *     <li>服务端是执行游戏逻辑的地方，比如你需要计算、获取或改变玩家的属性，你需要在服务端侧执行。</li>
     *     <li>客户端是处理用户输入、渲染的地方，比如你需要获取和处理玩家的输入，或者你需要渲染一些东西，你需要在客户端侧执行。<br>
     * </ul>
     * 比如你需要在服务端计算一些内容，入口部分应该这样写：
     * <pre>{@code
     * if (!player.level().isClientSide()) {
     *    // if块内的代码只会在服务端执行，在这里执行游戏逻辑
     *    // 如果你没有这个判断，你的逻辑会同时在服务端和客户端执行，也就是两次
     * }
     * }</pre>
     * <br>
     * 另外，当你需要在纯客户端的环境下写一些东西，你需要加上{@code @OnlyIn(Dist.CLIENT)}注解在类或方法上：
     * <pre>{@code
     * @OnlyIn(Dist.CLIENT)
     * public class ClientOnlyClass {
     *     // 这个类只会在客户端环境下被加载和使用，在这里执行一些纯客户端的操作，比如注册按键绑定和处理按键输入
     *     // 比如这是一个按键绑定，它不需要在服务端存在
     *     public static final KeyMapping EXAMPLE_KEY = new KeyMapping(
     *         "key." + StellarMod.MOD_ID + ".example",
     *         KeyConflictContext.IN_GAME,
     *         InputConstants.Type.KEYSYM,
     *         GLFW.GLFW_KEY_P,
     *         "key.categories." + StellarMod.MOD_ID
     *     );
     * }
     * }</pre>
     * 虽然这不是强制要求，但这是一个良好的实践，可以避免一些潜在的问题，比如错误的在服务端访问客户端内容。<br>
     * 另外mixin技术会通过这个注解来区分客户端和服务端的混入目标。<br>
     * @param player 一个玩家对象
     * @see net.minecraftforge.api.distmarker.OnlyIn
     * @see net.minecraftforge.api.distmarker.Dist
     * @see ExampleKeyMapping
     */
    @Deprecated
    public abstract void clientAndServerConcept(Player player);

    /**
     * <h3>数据同步的概念</h3>
     * 由于客户端和服务端的架构，双方的内容不会自动同步，举个例子：<br>
     * 服务端负责计算，现在它计算出在{@code (x, y, z)}的位置上长出了一朵花，如果它不通知客户端，
     * 客户端无法得知这个信息，也就无法在游戏中渲染这朵花。<br>
     * 因此，当你需要在服务端改变一些东西，并且需要让客户端知道这个改变。<br>
     * <br>
     * 类似的，服务端也需要知道客户端的一些信息，比如玩家的输入，玩家的界面状态等等。<br>
     * @param player 一个玩家对象
     * @see com.mojang.authlib.minecraft.client.MinecraftClient
     */
    @Deprecated
    public abstract void syncConcept(Player player);

    /**
     * <h3>网络通信的概念</h3>
     * 客户端和服务端之间的数据通信（包括数据同步）需要通过网络通信来实现。<br>
     * 比如你需要在客户端获取玩家的输入，并在服务端执行一些逻辑，你需要在客户端发送一个网络包到服务端，在服务端接收这个包并处理它。<br>
     * <br>
     * 从客户端发送数据包到服务端：
     * <pre>{@code
     * private void aClientMethod() {
     *     // 创建一个数据包对象，包含需要发送的数据
     *     ExamplePacket packet = new ExamplePacket(123);
     *     // 发送数据包到服务端
     *     ExampleNetwork.EXAMPLE_CHANNEL.sendToServer(packet);
     * }
     * }</pre>
     * 从服务端发送数据包到客户端：
     * <pre>{@code
     * private void aServerMethod() {
     *     // 创建一个数据包对象，包含需要发送的数据
     *     ExamplePacket packet = new ExamplePacket(123);
     *     // 服务器发送数据包到客户端，可以指定发送范围，比如这里演示发送给一个玩家
     *     ExampleNetwork.EXAMPLE_CHANNEL.send(
     *             PacketDistributor.PLAYER.with(() -> serverPlayer),
     *             packet
     *     );
     * }
     * }</pre>
     * @param player 一个玩家对象
     * @see PacketDistributor
     * @see ExamplePacket
     * @see ExampleNetwork
     */
    @Deprecated
    public abstract void networkConcept(Player player);
}

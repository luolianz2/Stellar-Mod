package com.luolian.stellarmod.example;

import net.minecraft.world.entity.player.Player;

/**
 * <h1>开发中的危险操作</h1>
 * 其中演示了在模组开发过程中可能会遇到的危险操作，需要引起注意。
 */
public abstract class DangerousOperation {
    /**
     * <h3>不是自己创建的资源，不要管它</h3>
     * 关于{@code player.level()}方法。<br>
     * 编译器可能会警告“{@code 在没有try-with-resources的情况下使用了AutoCloseable资源}”。<br>
     * 原因是{@code Level}类实现了{@code AutoCloseable}接口，编译器认为它是一个需要关闭的资源，但实际上它不是游戏中需要关闭的资源。<br>
     * 它的目的我们不需要了解，重点是我们不需要真的关闭它。
     * <pre>{@code
     * try (Level level = player.level()) {
     *     // 千万不要像这样使用try-with-resources包围，这不是我们需要关闭的资源，轻则存档损坏，重则游戏崩溃
     *     // 这是错误用法
     * }
     * }</pre>
     * @param player 一个玩家对象
     */
    @Deprecated
    public abstract void levelMethod(Player player);
}

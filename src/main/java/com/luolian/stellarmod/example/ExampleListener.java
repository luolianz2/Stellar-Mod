package com.luolian.stellarmod.example;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>事件监听器的示例类</h1>
 * 其中演示了事件监听器的使用方法，事件监听器是Forge模组开发中非常重要的一部分，
 * 通过它你可以监听游戏中的各种事件，并在事件发生时执行相应的逻辑。<br>
 * <br>
 * 通过{@code @Mod.EventBusSubscriber}注解使用Forge事件总线不需要在模组主类中注册，
 * 在{@code @Mod.EventBusSubscriber}注解中指定modid变量即可<br>
 * <br>
 * 一般不需要了解其它使用形式
 */
@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID)
public class ExampleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleListener.class);

    /**
     * 一般方法是静态的，但也可以是实例方法，取决于具体实现<br>
     * 以下是示例方法，方法定义有两点要求：
     * <ul>
     *     <li>方法形参必须只能有一个</li>
     *     <li>方法不需要返回结果</li>
     * </ul>
     * @param event 你想要监听的事件
     * @see BlockEvent
     * @see net.minecraftforge.eventbus.api.Event
     */
    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer(); // 获取破坏方块的玩家
        Block block = event.getState().getBlock(); // 获取被破坏的方块
        Level level = (Level) event.getLevel(); // 获取事件发生的世界

        LOGGER.info(
                "玩家 {} 在世界 {} 破坏了方块 {}",
                player.getName(),
                level.dimension().location(),
                block.getName()
        );
    }
}

package com.luolian.stellarmod.client.key;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.client.screen.toolCore.ToolCoreRadialMenuScreen;
import com.luolian.stellarmod.common.matrix.CreativeFlightEffect;
import com.luolian.stellarmod.network.FlightBoostPacket;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StellarKeyMapping {

    public static final Lazy<KeyMapping> OPEN_RADIAL_MENU = Lazy.of(() -> new KeyMapping(
            "key.stellarmod.open_radial_menu",  //控件描述
            InputConstants.Type.KEYSYM, //键盘按键
            GLFW.GLFW_KEY_R,            //默认r键
            "key.categories.stellarmod" //类别
    ));

    /** 飞行快速模式切换：按下后循环切换加速档位 (0→1→2→3→0) */
    public static final Lazy<KeyMapping> FLIGHT_BOOST_CYCLE = Lazy.of(() -> new KeyMapping(
            "key.stellarmod.flight_boost_cycle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.stellarmod"
    ));

    /** 飞行快速模式关闭：按下后直接重置为 0 档 */
    public static final Lazy<KeyMapping> FLIGHT_BOOST_OFF = Lazy.of(() -> new KeyMapping(
            "key.stellarmod.flight_boost_off",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.stellarmod"
    ));

    private static boolean wasKeyDown = false;
    private static boolean justClosed = false; //防止关闭后立即重新打开

    @Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_RADIAL_MENU.get());
            event.register(FLIGHT_BOOST_CYCLE.get());
            event.register(FLIGHT_BOOST_OFF.get());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        //确保 onClientTick 方法内的逻辑只在每个客户端 Tick 的结束阶段执行一次，而不是在开始阶段也执行
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        //如果打开了任何 GUI，不处理
        if (mc.screen != null) {
            wasKeyDown = false;
            return;
        }

        KeyMapping keyMapping = OPEN_RADIAL_MENU.get();
        boolean isKeyDown = keyMapping.isDown();

        //如果刚刚因执行动作关闭了轮盘，则忽略当前按下的按键，直到按键释放
        if (justClosed) {
            if (!isKeyDown) {
                justClosed = false; //按键已释放，重置标记
            }
            wasKeyDown = isKeyDown;
            return;
        }

        //按下瞬间，手持工具核心时打开轮盘
        //isKeyDown	当前帧按键是否处于按下状态
        //!wasKeyDown	上一帧按键是否没有按下
        if (isKeyDown && !wasKeyDown) { //仅在按键从"未按下"变为"按下"的那一瞬间触发一次操作，而不是在按键持续按住期间每帧都反复触发
            ItemStack held = mc.player.getMainHandItem();
            if (held.getItem() instanceof ToolCoreItem) {
                mc.setScreen(new ToolCoreRadialMenuScreen(held));
            }
        }

        //加速飞行档位切换
        handleFlightBoost(mc.player);

        wasKeyDown = isKeyDown;
    }

    /**
     * 处理飞行加速档位切换逻辑。
     * <ul>
     *   <li><b>循环键 (X)</b>：档位 0→1→2→3→0 循环</li>
     *   <li><b>关闭键 (Z)</b>：一键重置为 0 档</li>
     * </ul>
     * 仅在玩家拥有已启用的飞行矩阵且可用档位≥1 时生效。
     *
     * @param player 当前客户端玩家
     */
    private static void handleFlightBoost(Player player) {
        //consumeClick()检测并消费一次点击。如果该按键从上一帧到当前帧刚好被按下过一次，则返回 true，并将内部状态标记为“已消费”，之后再次调用会返回 false，直到下一次新的按下
        boolean cyclePressed = FLIGHT_BOOST_CYCLE.get().consumeClick();
        boolean offPressed = FLIGHT_BOOST_OFF.get().consumeClick();

        if (!cyclePressed && !offPressed) return;

        //扫描背包获取最大可用档位
        int maxTier = CreativeFlightEffect.getMaxBoostTier(player);
        if (maxTier <= 0) return; //无飞行矩阵或等级不足，静默忽略

        int newTier;
        if (offPressed) {
            //关闭键 → 重置为 0 档
            newTier = 0;
        } else {
            //循环键 → 档位 +1，超过上限后回到 0，上面已经做了判断，如果循环和关闭键都没被按下则会返回
            int current = CreativeFlightEffect.getBoostTier(player);
            newTier = (current + 1 > maxTier) ? 0 : current + 1;
        }

        //更新客户端本地状态
        CreativeFlightEffect.setBoostTier(player, newTier);

        //同步到服务端（服务端会二次验证后存储）
        StellarNetworkHandler.INSTANCE.sendToServer(new FlightBoostPacket(newTier));

        //在物品栏上方显示当前档位提示
        /*
            player.displayClientMessage(Component message, boolean actionBar)
            作用：向该玩家客户端发送一条仅自己可见的消息。
                参数 message：要显示的文本组件（支持富文本、翻译、颜色等）。
                参数 actionBar：
                    false：消息显示在聊天栏（Chat）中，会保留在聊天记录里。
                    true：消息显示在动作栏（ActionBar）上，位于物品栏上方、血量/饥饿值下方，通常数秒后自动消失，不会进入聊天记录。
            Component.translatable(String key, Object... args)
                创建一个可本地化的翻译组件，它会根据客户端语言设置自动从语言文件（如 en_us.json、zh_cn.json）中获取对应文本。
                参数 key 是一个翻译键（translation key），例如 "creative_flight.boost.tier.1"。
                这里使用 CreativeFlightEffect.getBoostTierTranslationKey(newTier) 动态生成翻译键，不同等级返回不同键名（如 "boost.tier.low"、"boost.tier.high"）
        */
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        CreativeFlightEffect.getBoostTierTranslationKey(newTier)
                ),
                true //true = actionBar 位置
        );
    }

    /**
     * 由 RadialMenuScreen 在因执行动作而关闭时调用，设置冷却标记。
     */
    public static void notifyClosedFromAction() {
        justClosed = true;
    }
}
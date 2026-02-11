package com.luolian.stellarmod.example;

import com.luolian.stellarmod.StellarMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * <h1>按键绑定的示例类</h1>
 * 其中演示了如何注册一个按键绑定，以及一些相关的概念。
 */
@Mod.EventBusSubscriber(
        modid = StellarMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD, // 注意：RegisterKeyMappingsEvent是一个MOD事件，而不是一个FORGE事件，指定错误的bus将导致你的代码永远不会被调用。
        value = Dist.CLIENT // 纯客户端的事件
)
public class ExampleKeyMapping {
    public static final Lazy<KeyMapping> EXAMPLE_KEY = Lazy.of(() -> new KeyMapping(
            "key." + StellarMod.MOD_ID + ".example", // 按键绑定的翻译键
            KeyConflictContext.IN_GAME, // 按键冲突上下文，这里表示这个按键绑定仅在游戏内有效
            InputConstants.Type.KEYSYM, // 按键类型，这里表示这是一个键盘按键
            GLFW.GLFW_KEY_P, // 默认按键，这里是P键
            "key.categories." + StellarMod.MOD_ID // 按键分类的翻译键
    ));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EXAMPLE_KEY.get()); // 注册按键绑定
    }
}

@Mod.EventBusSubscriber(
        modid = StellarMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, // 注意：TickEvent是一个FORGE事件，而不是一个MOD事件，指定错误的bus将导致你的代码永远不会被调用。
        value = Dist.CLIENT // 纯客户端的事件
)
class ClientEvent {
    /**
     * 检测按键状态的方法有以下两个：
     * <ul>
     *     <li>{@code isDown()}：当按键被按下时返回true，持续按住时每tick都会返回true。</li>
     *     <li>{@code consumeClick()}：当按键被按下时返回true，并且只会返回一次，直到按键被释放后再次按下才会再次返回true。</li>
     * </ul>
     * @param event 客户端tick事件
     */
    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (ExampleKeyMapping.EXAMPLE_KEY.get().consumeClick()) {
                // do something when the key is pressed
            }
        }
    }
}

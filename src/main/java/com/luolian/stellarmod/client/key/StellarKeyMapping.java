package com.luolian.stellarmod.client.key;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.client.screen.toolCore.ToolCoreRadialMenuScreen;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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

    private static boolean wasKeyDown = false;
    private static boolean justClosed = false; //防止关闭后立即重新打开

    @Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_RADIAL_MENU.get());
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

        wasKeyDown = isKeyDown;
    }

    /**
     * 由 RadialMenuScreen 在因执行动作而关闭时调用，设置冷却标记。
     */
    public static void notifyClosedFromAction() {
        justClosed = true;
    }
}
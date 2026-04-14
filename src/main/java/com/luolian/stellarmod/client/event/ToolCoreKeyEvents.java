package com.luolian.stellarmod.client.event;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.client.screen.toolCore.RadialMenuScreen;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, value = Dist.CLIENT)
public class ToolCoreKeyEvents {

    private static boolean wasKeyDown = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof ToolCoreItem)) return;

        boolean isRDown = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;

        //刚按下 R 键时打开轮盘
        if (isRDown && !wasKeyDown) {
            mc.setScreen(new RadialMenuScreen(held));
        }

        wasKeyDown = isRDown;
    }
}
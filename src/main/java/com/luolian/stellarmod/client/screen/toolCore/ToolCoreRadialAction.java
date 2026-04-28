package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

import static com.luolian.stellarmod.StellarMod.textureLocation;

//轮盘槽位定义与数据
public enum ToolCoreRadialAction {
    PICKAXE(0, "pickaxe", textureLocation("gui/pickaxe.png"),
            (stack, player) -> ToolCoreItem.setActiveType(stack, ToolCoreItem.ToolType.PICKAXE)),
    AXE(1, "axe", textureLocation("gui/axe.png"),
            (stack, player) -> ToolCoreItem.setActiveType(stack, ToolCoreItem.ToolType.AXE)),
    SHOVEL(2, "shovel", textureLocation("gui/shovel.png"),
            (stack, player) -> ToolCoreItem.setActiveType(stack, ToolCoreItem.ToolType.SHOVEL)),
    SWORD(3, "sword", textureLocation("gui/sword.png"),
            (stack, player) -> ToolCoreItem.setActiveType(stack, ToolCoreItem.ToolType.SWORD)),
    HOE(4, "hoe", textureLocation("gui/hoe.png"),
            (stack, player) -> ToolCoreItem.setActiveType(stack, ToolCoreItem.ToolType.HOE)),
    SETTINGS(5, "settings", textureLocation("gui/settings.png"),
            (stack, player) -> {
                //打开设置屏幕
                Minecraft.getInstance().setScreen(new ToolCoreModifierSettingsScreen(stack));
            }),
    EMPTY_1(6, "empty", null, (s, p) -> {}),
    MATRIX(7, "matrix", textureLocation("gui/matrix.png"),
            (stack, player) -> {
                if (player.level().isClientSide) {
                    //打开矩阵屏幕
                    Minecraft.getInstance().setScreen(new ToolCoreMatrixModuleScreen(stack));
                }
            });

    private final int index;
    private final String id;
    private final ResourceLocation icon;
    private final BiConsumer<ItemStack, Player> action;

    ToolCoreRadialAction(int index, String id, ResourceLocation icon, BiConsumer<ItemStack, Player> action) {
        this.index = index;
        this.id = id;
        this.icon = icon;
        this.action = action;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public void execute(ItemStack stack, Player player) {
        action.accept(stack, player);
    }

    /**
     * 根据槽位索引获取对应的 RadialAction。
     * 如果索引非法（小于0或大于等于枚举常量数），则返回默认的 EMPTY_1。
     * 该方法保证了在任何情况下都不会因索引越界而崩溃。
     *
     * @param index 轮盘槽位索引 (0~7)
     * @return 对应的 RadialAction 枚举实例，非法索引时返回 EMPTY_1
     */
    public static ToolCoreRadialAction fromIndex(int index) {
        //快速边界检查，避免无意义的遍历
        if (index < 0 || index >= values().length) {
            return EMPTY_1;
        }
        //遍历查找匹配的 index
        for (ToolCoreRadialAction action : values()) {
            if (action.index == index) {
                return action;
            }
        }
        //理论上不会执行到这里，但作为 fallback 返回空槽位
        return EMPTY_1;
    }
}
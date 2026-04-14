package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

import static com.luolian.stellarmod.StellarMod.textureLocation;

//轮盘槽位定义与数据
public enum RadialAction {
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
    SETTINGS(5, "settings", textureLocation("gui/compass.png"),
            (stack, player) -> { /* 打开设置界面（后续扩展） */ }),
    EMPTY_1(6, "empty", null, (s, p) -> {}),
    EMPTY_2(7, "empty", null, (s, p) -> {});

    private final int index;
    private final String id;
    private final ResourceLocation icon;
    private final BiConsumer<ItemStack, Player> action;

    RadialAction(int index, String id, ResourceLocation icon, BiConsumer<ItemStack, Player> action) {
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

    public static RadialAction fromIndex(int index) {
        for (RadialAction a : values()) if (a.index == index) return a;
        return EMPTY_1;
    }
}
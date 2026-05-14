package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.server.item.custom.toolcore.ToolCoreItem;
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
    MODIFIERS(5, "modifiers", textureLocation("gui/modifiers.png"),
            (stack, player) -> {
                //打开副词条设置屏幕
                /*
                  getInstance() 是典型的单例模式方法，它的作用是获取这个类在游戏运行时唯一存在的那一个实例。
                  在模组开发中，Minecraft 类代表了整个游戏客户端本身，它掌管着窗口、玩家、世界、画面等所有核心状态。因为这个类太重要了，而且一个游戏进程里只需要一个客户端，
                    所以它被设计成单例——你不能随便 new Minecraft()，只能通过 Minecraft.getInstance() 来拿到这个唯一的实例。
                  简单类比一下：
                    new Car() —— 你可以造很多辆车
                    Car.getInstance() —— 只有一辆独一无二的车，这个方法就是给你车钥匙，让你能用这辆车
                  所以 Minecraft.getInstance() 的意思就是：“把当前正在运行的这个游戏客户端给我”。拿到它之后，就可以调用它的各种功能，比如切换界面、获取玩家、发送消息等。
                */
                Minecraft.getInstance().setScreen(new ToolCoreModifierSettingsScreen(stack));
            }),
    MATRIX(6, "matrix", textureLocation("gui/matrix.png"),
            (stack, player) -> {
                if (player.level().isClientSide) {
                    //打开矩阵屏幕
                    Minecraft.getInstance().setScreen(new ToolCoreMatrixModuleScreen(stack));
                }
            }),
    SETTINGS(7, "settings", textureLocation("gui/settings.png"),
            (stack, player) -> {
                //打开设置屏幕
                Minecraft.getInstance().setScreen(new ToolCoreSettingsScreen(stack));
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
     * 如果索引非法（小于0或大于等于枚举常量数），则返回 null。
     * 该方法保证了在任何情况下都不会因索引越界而崩溃。
     *
     * @param index 轮盘槽位索引 (0~7)
     * @return 对应的 RadialAction 枚举实例，非法索引时返回 null
     */
    public static ToolCoreRadialAction fromIndex(int index) {
        //快速边界检查，避免无意义的遍历
        if (index < 0 || index >= values().length) {
            return null;
        }
        //遍历查找匹配的 index
        for (ToolCoreRadialAction action : values()) {
            if (action.index == index) {
                return action;
            }
        }
        return null;
    }

    /**
     * 根据功能 ID 查找对应的 RadialAction。
     *
     * @param id 功能标识符字符串（如 "pickaxe", "modifiers" 等）
     * @return 对应的枚举实例，未找到则返回 null
     */
    public static ToolCoreRadialAction fromId(String id) {
        if (id == null) return null;
        for (ToolCoreRadialAction action : values()) {
            if (action.id.equals(id)) {
                return action;
            }
        }
        return null;
    }

    /**
     * 判断给定 ID 是否为工具形态（索引 0~4）。
     */
    public static boolean isToolType(String id) {
        return "pickaxe".equals(id) || "axe".equals(id) || "shovel".equals(id)
                || "sword".equals(id) || "hoe".equals(id);
    }
}
package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.client.config.RadialConfigStorage;
import com.luolian.stellarmod.client.config.ToolCoreRadialState;
import com.luolian.stellarmod.client.key.StellarKeyMapping;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.toolcore.SwitchToolTypePacket;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ToolCoreRadialMenuScreen extends Screen {

    //被选框右下角的标记
    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/widgets.png");
    private static final int SLOT_COUNT = 8;        //轮盘槽位数
    private static final int RADIUS = 80;          //轮盘半径
    private static final int SLOT_SIZE = 24;       //槽位图标大小
    private static final int CENTER_X = 0;         //将基于屏幕中心计算，此处为偏移基准
    private static final int CENTER_Y = 0;

    private final ItemStack toolStack;
    private int highlightedSlot = -1;               //当前鼠标指向的槽位索引
    private int currentRadialIndex;                 //当前显示的轮盘索引

    public ToolCoreRadialMenuScreen(ItemStack toolStack) {
        super(Component.translatable("screen.stellarmod.radial_menu"));
        this.toolStack = toolStack;
        this.minecraft = Minecraft.getInstance();
        //按R键打开时始终从索引1（默认轮盘）开始
        this.currentRadialIndex = 1;
    }

    @Override
    public boolean isPauseScreen() {
        return false; //不暂停游戏
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //确保渲染状态干净，开启混合以避免与其他模组的渲染冲突
        RenderSystem.enableBlend();

        //不渲染默认背景，保留游戏画面
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        //更新高亮槽位
        updateHighlightedSlot(mouseX, mouseY, centerX, centerY);

        //绘制所有槽位
        for (int i = 0; i < SLOT_COUNT; i++) {
            drawSlot(graphics, centerX, centerY, i, highlightedSlot == i);
        }

        //绘制中心图标（当前工具核心图标）
        renderCenterIcon(graphics, centerX, centerY);

        //如果高亮槽位有效，显示其名称
        if (highlightedSlot >= 0) {
            String actionId = RadialConfigStorage.getSlotAction(currentRadialIndex, highlightedSlot);
            if (actionId != null) {

                //尝试将功能 ID 转换为 ToolCoreRadialAction 枚举常量（比如镐）
                //若 actionId 不对应任何枚举值，则 action 为 null
                ToolCoreRadialAction action = ToolCoreRadialAction.fromId(actionId);
                String displayKey;
                if (action != null) {
                    displayKey = action.getId();
                } else {
                    //对于非枚举的功能（如副词条/矩阵效果），尝试作为翻译键显示
                    displayKey = actionId;
                }
                graphics.drawCenteredString(font, Component.translatable("action.stellarmod." + displayKey),
                        centerX, centerY + RADIUS + 20, 0xFFFFFF);
            }
        }

        //显示当前轮盘索引指示器（仅在索引 > 1 时显示）
        if (currentRadialIndex > 1) {
            String indexText = "[" + currentRadialIndex + "/" + RadialConfigStorage.getRadialCount() + "]";
            graphics.drawString(font, indexText, centerX - font.width(indexText) / 2,
                    centerY - RADIUS - 30, 0xAAAAAA);
        }

        //恢复渲染状态，避免对后续渲染造成影响
        RenderSystem.disableBlend();
    }

    private void updateHighlightedSlot(int mouseX, int mouseY, int centerX, int centerY) {
        int dx = mouseX - centerX;
        int dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        //只有鼠标距离中心足够远时才高亮槽位
        if (distance > RADIUS * 0.5 && distance < RADIUS * 1.5) {
            //计算角度 (0° 指向正上方，顺时针增加)
            double angle = Math.atan2(dy, dx) * 180.0 / Math.PI;
            //转换：使 0° 对应正上（-90° 偏移），且顺时针
            angle = (angle + 90 + 360) % 360;
            //每个槽位占 45°
            int slot = (int) ((angle + 22.5) / 45) % 8;
            highlightedSlot = slot;
        } else {
            highlightedSlot = -1;
        }
    }

    private void drawSlot(GuiGraphics graphics, int centerX, int centerY, int slotIndex, boolean highlighted) {
        //计算角度：槽位 0 在顶部，顺时针
        double angle = Math.toRadians(slotIndex * 45.0 - 90); //-90° 使槽位0在正上方
        int x = centerX + (int) (RADIUS * Math.cos(angle)) - SLOT_SIZE / 2;
        int y = centerY + (int) (RADIUS * Math.sin(angle)) - SLOT_SIZE / 2;

        //从配置中读取该槽位的功能 ID
        String actionId = RadialConfigStorage.getSlotAction(currentRadialIndex, slotIndex);
        ToolCoreRadialAction action = ToolCoreRadialAction.fromId(actionId);

        //绘制背景（方形）
        int bgColor = highlighted ? 0x80FFFFFF : 0x40000000;
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
        //绘制边框
        graphics.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, highlighted ? 0xFFFFFFFF : 0xFFAAAAAA);

        //绘制图标
        if (action != null && action.getIcon() != null) {
            //直接使用 GuiGraphics.blit，无需手动设置纹理，避免状态污染
            graphics.blit(action.getIcon(), x + 4, y + 4, 0, 0, 16, 16, 16, 16);
        } else if (actionId != null && !actionId.isEmpty()) {

            //对于非枚举功能（副词条/矩阵效果），绘制首字母作为临时图标
            //.substring(0, 1)截取字符串的第一个字符（索引 0 到 1，不包括 1），得到长度为 1 的字符串，toUpperCase()：将该字符转换为大写字母
            String letter = actionId.substring(0, 1).toUpperCase();
            graphics.drawCenteredString(font, letter, x + SLOT_SIZE / 2, y + SLOT_SIZE / 2 - 4, 0xFFFFFF);
        }

        //如果是当前激活的形态，绘制一个标记
        ToolCoreItem.ToolType activeType = ToolCoreItem.getActiveType(toolStack);
        if (action != null && matchesActiveType(action, activeType)) {
            graphics.blit(WIDGETS, x + SLOT_SIZE - 8, y + SLOT_SIZE - 8, 0, 0, 8, 8, 256, 256);
        }
    }

    private boolean matchesActiveType(ToolCoreRadialAction action, ToolCoreItem.ToolType activeType) {
        return switch (action) {
            case PICKAXE -> activeType == ToolCoreItem.ToolType.PICKAXE;
            case AXE -> activeType == ToolCoreItem.ToolType.AXE;
            case SHOVEL -> activeType == ToolCoreItem.ToolType.SHOVEL;
            case SWORD -> activeType == ToolCoreItem.ToolType.SWORD;
            case HOE -> activeType == ToolCoreItem.ToolType.HOE;
            default -> false;
        };
    }

    //绘制中心图标
    private void renderCenterIcon(GuiGraphics graphics, int centerX, int centerY) {
        //主动刷新缓冲区，确保物品渲染在干净的状态下进行（虽然 renderItem 内部也会 flush，但显式调用更安全）
        graphics.flush();
        graphics.renderItem(toolStack, centerX - 8, centerY - 8);
    }

    //鼠标点击逻辑
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && highlightedSlot >= 0) {
            executeAction(highlightedSlot);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    //执行轮盘槽位对应的动作
    private void executeAction(int slotIndex) {
        String actionId = RadialConfigStorage.getSlotAction(currentRadialIndex, slotIndex);
        if (actionId == null) {
            //空槽位，关闭轮盘
            StellarKeyMapping.notifyClosedFromAction();
            onClose();
            return;
        }

        ToolCoreRadialAction action = ToolCoreRadialAction.fromId(actionId);
        Player player = minecraft.player;
        if (player == null) {
            StellarKeyMapping.notifyClosedFromAction();
            onClose();
            return;
        }

        if (action != null) {
            //枚举中定义的标准动作
            if (ToolCoreRadialAction.isToolType(actionId)) {
                //前五个是工具形态，发送网络包切换形态
                //getIndex() 只看这个动作是什么，不看它放在哪个槽位，是硬编码的，比如说镐这个动作的getIndex返回值永久为0，其被枚举在ToolCoreRadialAction类中
                ToolCoreItem.ToolType targetType = ToolCoreItem.ToolType.values()[action.getIndex()];
                StellarNetworkHandler.INSTANCE.sendToServer(new SwitchToolTypePacket(targetType));
                StellarKeyMapping.notifyClosedFromAction();
                onClose();
            } else {
                //面板入口：MODIFIERS / MATRIX / SETTINGS
                action.execute(toolStack, player);
                StellarKeyMapping.notifyClosedFromAction();
            }
        } else {
            //非枚举功能（如副词条/矩阵效果），暂时关闭轮盘（后续扩展）
            StellarKeyMapping.notifyClosedFromAction();
            onClose();
        }
    }

    /**
     * 处理轮盘打开期间的按键事件。
     * <p>
     * 左右 Shift：将当前显示的轮盘切到下一个有内容的索引（循环），
     * 从而实现"一个 R 键浏览多个轮盘页"的效果。
     * <p>
     * 其他按键交由父类处理。
     *
     * @param keyCode   被按下的键的 GLFW 键码（如 {@code GLFW.GLFW_KEY_LEFT_SHIFT}）
     * @param scanCode  平台相关的键盘扫描码（由操作系统或硬件提供，表示按键的物理位置，而不是键的功能）
     * @param modifiers 当前有效的修饰键位掩码（Ctrl/Alt/Shift 等组合，可以理解为复合键，比如说Ctrl+A）
     * @return true 表示事件已消费，不再向后续监听器传递
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //Shift 切换轮盘索引
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            ToolCoreRadialState.switchToNextRadial();
            currentRadialIndex = ToolCoreRadialState.getCurrentRadialIndex();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        //获取当前配置的轮盘键位
        KeyMapping keyMapping = StellarKeyMapping.OPEN_RADIAL_MENU.get(); //获取玩家在控制设置中自定义的轮盘键位
        InputConstants.Key key = keyMapping.getKey();   //返回该键位当前绑定的具体按键

        //如果释放的键正是轮盘键，则执行对应动作
        //key.getType() == InputConstants.Type.KEYSYM确保是键盘按键
        //key.getValue() == keyCode判断释放的按键是否正是轮盘键
        if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() == keyCode) {
            if (highlightedSlot >= 0) {   //如果释放按键时鼠标正悬停在某个有效槽位上，则执行该槽位对应的动作
                executeAction(highlightedSlot);
            } else {
                //没有高亮槽位，直接关闭轮盘
                StellarKeyMapping.notifyClosedFromAction();
                onClose();
            }
            return true;    //表示该按键事件已被消费，不再传递给后续的按键处理器
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    //屏幕关闭时调用
    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
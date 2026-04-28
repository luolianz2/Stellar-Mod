package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.client.key.StellarKeyMapping;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.SwitchToolTypePacket;
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

    public ToolCoreRadialMenuScreen(ItemStack toolStack) {
        super(Component.translatable("screen.stellarmod.radial_menu"));
        this.toolStack = toolStack;
        this.minecraft = Minecraft.getInstance();
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
            ToolCoreRadialAction action = ToolCoreRadialAction.fromIndex(highlightedSlot);
            if (!action.getId().equals("empty")) {
                graphics.drawCenteredString(font, Component.translatable("action.stellarmod." + action.getId()),
                        centerX, centerY + RADIUS + 20, 0xFFFFFF);
            }
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

        ToolCoreRadialAction action = ToolCoreRadialAction.fromIndex(slotIndex);

        //绘制背景（方形）
        int bgColor = highlighted ? 0x80FFFFFF : 0x40000000;
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
        //绘制边框
        graphics.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, highlighted ? 0xFFFFFFFF : 0xFFAAAAAA);

        //绘制图标
        if (action.getIcon() != null) {
            //直接使用 GuiGraphics.blit，无需手动设置纹理，避免状态污染
            graphics.blit(action.getIcon(), x + 4, y + 4, 0, 0, 16, 16, 16, 16);
        }

        //如果是当前激活的形态，绘制一个标记
        ToolCoreItem.ToolType activeType = ToolCoreItem.getActiveType(toolStack);
        if (matchesActiveType(action, activeType)) {
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
        ToolCoreRadialAction action = ToolCoreRadialAction.fromIndex(slotIndex);
        if (!action.getId().equals("empty")) {
            Player player = minecraft.player;
            if (player != null) {
                if (action.ordinal() < 5) {
                    //前五个是工具形态，发送网络包切换形态
                    ToolCoreItem.ToolType targetType = ToolCoreItem.ToolType.values()[action.ordinal()];
                    StellarNetworkHandler.INSTANCE.sendToServer(new SwitchToolTypePacket(targetType));
                    //通知按键系统：轮盘因执行动作而关闭，防止立即重新打开
                    StellarKeyMapping.notifyClosedFromAction();
                    onClose();
                } else if (action == ToolCoreRadialAction.SETTINGS) {
                    //打开副词条设置屏幕（新屏幕会覆盖当前屏幕，无需手动 onClose）
                    Minecraft.getInstance().setScreen(new ToolCoreModifierSettingsScreen(toolStack));
                    //仍需通知按键系统，防止释放按键时误触发再次打开轮盘
                    StellarKeyMapping.notifyClosedFromAction();
                } else if (action == ToolCoreRadialAction.MATRIX) {
                    Minecraft.getInstance().setScreen(new ToolCoreMatrixModuleScreen(toolStack));
                    StellarKeyMapping.notifyClosedFromAction(); // 跳过关闭，新屏幕会接管
                } else {
                    //对于其他非形态槽位（预留扩展），调用枚举中定义的自定义逻辑
                    action.execute(toolStack, player);
                    StellarKeyMapping.notifyClosedFromAction();
                    onClose();
                }
            }
        } else {
            //如果点击的是空槽位，也关闭轮盘
            StellarKeyMapping.notifyClosedFromAction();
            onClose();
        }
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
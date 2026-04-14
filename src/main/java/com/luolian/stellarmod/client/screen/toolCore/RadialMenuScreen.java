package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.SwitchToolTypePacket;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class RadialMenuScreen extends Screen {

    //被选框右下角的标记
    private static final ResourceLocation WIDGETS =  ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/widgets.png");
    private static final int SLOT_COUNT = 8;        //轮盘槽位数
    private static final int RADIUS = 80;          //轮盘半径
    private static final int SLOT_SIZE = 24;       //槽位图标大小
    private static final int CENTER_X = 0;         //将基于屏幕中心计算，此处为偏移基准
    private static final int CENTER_Y = 0;

    private final ItemStack toolStack;
    private int highlightedSlot = -1;               //当前鼠标指向的槽位索引
    private boolean keyWasDown = true;              //用于检测R键松开

    public RadialMenuScreen(ItemStack toolStack) {
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
            RadialAction action = RadialAction.fromIndex(highlightedSlot);
            if (!action.getId().equals("empty")) {
                graphics.drawCenteredString(font, Component.translatable("action.stellarmod." + action.getId()),
                        centerX, centerY + RADIUS + 20, 0xFFFFFF);
            }
        }
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
            // 每个槽位占 45°
            int slot = (int) ((angle + 22.5) / 45) % 8;
            highlightedSlot = slot;
        } else {
            highlightedSlot = -1;
        }
    }

    private void drawSlot(GuiGraphics graphics, int centerX, int centerY, int slotIndex, boolean highlighted) {
        //计算角度：槽位 0 在顶部，顺时针
        double angle = Math.toRadians(slotIndex * 45.0 - 90); // -90° 使槽位0在正上方
        int x = centerX + (int) (RADIUS * Math.cos(angle)) - SLOT_SIZE / 2;
        int y = centerY + (int) (RADIUS * Math.sin(angle)) - SLOT_SIZE / 2;

        RadialAction action = RadialAction.fromIndex(slotIndex);

        //绘制背景（方形）
        int bgColor = highlighted ? 0x80FFFFFF : 0x40000000;
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
        //绘制边框
        graphics.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, highlighted ? 0xFFFFFFFF : 0xFFAAAAAA);

        //绘制图标
        if (action.getIcon() != null) {
            RenderSystem.setShaderTexture(0, action.getIcon());
            graphics.blit(action.getIcon(), x + 4, y + 4, 0, 0, 16, 16, 16, 16);
        }

        //如果是当前激活的形态，绘制一个标记
        ToolCoreItem.ToolType activeType = ToolCoreItem.getActiveType(toolStack);
        if (matchesActiveType(action, activeType)) {
            graphics.blit(WIDGETS, x + SLOT_SIZE - 8, y + SLOT_SIZE - 8, 0, 0, 8, 8, 256, 256);
        }
    }

    private boolean matchesActiveType(RadialAction action, ToolCoreItem.ToolType activeType) {
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
        //绘制工具核心物品的图标
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

    private void executeAction(int slotIndex) {
        RadialAction action = RadialAction.fromIndex(slotIndex);
        if (!action.getId().equals("empty")) {
            Player player = minecraft.player;
            if (player != null) {
                if (action.ordinal() < 5) { //前五个是工具形态
                    ToolCoreItem.ToolType targetType = ToolCoreItem.ToolType.values()[action.ordinal()];
                    //发送包到服务端
                    StellarNetworkHandler.INSTANCE.sendToServer(new SwitchToolTypePacket(targetType));
                } else {
                    //处理其他操作（如设置）
                    action.execute(toolStack, player);
                }
            }
        }
        onClose();
    }

    @Override
    public void tick() {
        super.tick();
        //检测 R 键松开，关闭轮盘
        boolean keyDown = GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (!keyDown && keyWasDown) {
            onClose();
        }
        keyWasDown = keyDown;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            //不处理按下，等待松开
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            if (highlightedSlot >= 0) { //如果有高亮槽位，执行对应操作
                executeAction(highlightedSlot);
            } else {    //否则直接关闭
                onClose();
            }
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
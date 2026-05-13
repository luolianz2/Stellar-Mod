package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.client.config.RadialConfigStorage;
import com.luolian.stellarmod.client.config.ToolCoreRadialState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * 轮盘槽位配置子面板。
 * <p>
 * 布局：
 * - 中央：当前索引的轮盘可视化（8 个可点击槽位）
 * - 右侧：候选功能图标区 + 移除区
 * - 左侧：切换快捷键说明 + 清空当前轮盘按钮（与右侧对称）
 * - 底部：鼠标悬停描述 + 索引切换控件(-/数字/+)
 * - 左下：保存并返回 + 默认
 * <p>
 * 交互模式：点击选中槽位，再点击候选区图标分配 / 点击移除区清空。
 * <p>
 * 校验规则：至少一个轮盘的某个槽位必须分配"设置"功能，否则阻止保存。
 */
public class ToolCoreRadialSlotConfigScreen extends Screen {

    private static final int SLOT_COUNT = 8;
    private static final int RADIUS = 70;
    private static final int SLOT_SIZE = 24;

    private final ItemStack toolStack;
    private final Screen parent;                    //返回的父屏幕（设置面板）
    private int currentConfigIndex;                 //当前正在编辑的轮盘索引
    private int selectedSlot = -1;                  //当前选中的槽位（-1 表示未选中）
    private boolean showSettingsWarning = false;    //是否显示"设置必须存在"警告

    //候选区
    private final List<String> availableActions = new ArrayList<>();
    private static final int CANDIDATE_COLS = 2;    //候选列数
    private static final int CANDIDATE_ITEM_SIZE = 22;
    private int candidateX, candidateY;
    private int candidateWidth, candidateHeight;

    //移除区
    private int removeZoneX, removeZoneY;
    private static final int REMOVE_ZONE_WIDTH = 80;
    private static final int REMOVE_ZONE_HEIGHT = 30;

    //左侧元素位置（与右侧对称）
    private int leftColumnX;    //左列x
    private int leftClearButtonY;

    //轮盘区域位置（在 render 中计算）
    private int radialCenterX, radialCenterY;

    //当前鼠标悬停在哪个区域（用于底部描述）
    private String hoverDescription = "";

    //索引范围控件（居中）
    private int indexNumberCenterX, indexNumberY;

    public ToolCoreRadialSlotConfigScreen(ItemStack toolStack, Screen parent) {
        super(Component.translatable("screen.stellarmod.tool_core.settings.radial_slot_config"));
        this.toolStack = toolStack;
        this.parent = parent;
        this.currentConfigIndex = ToolCoreRadialState.getCurrentRadialIndex();

        //构建可用功能列表
        for (ToolCoreRadialAction action : ToolCoreRadialAction.values()) {
            availableActions.add(action.getId());
        }
    }

    @Override
    protected void init() {
        super.init();
        this.selectedSlot = -1;
        this.hoverDescription = "";
        this.showSettingsWarning = false;

        radialCenterX = this.width / 2;
        radialCenterY = this.height / 2 - 10;

        //右侧候选区
        candidateX = radialCenterX + RADIUS + 45;
        candidateY = radialCenterY - 100;
        //ceil向上取整，计算需要多少行才能把所有动作摆下
        int rows = (int) Math.ceil((double) availableActions.size() / CANDIDATE_COLS);
        candidateWidth = CANDIDATE_COLS * CANDIDATE_ITEM_SIZE + 10;
        candidateHeight = rows * CANDIDATE_ITEM_SIZE + 10;

        //右侧移除区（候选区下方）
        removeZoneX = candidateX + (candidateWidth - REMOVE_ZONE_WIDTH) / 2;
        removeZoneY = candidateY + candidateHeight + 15;

        //左侧元素位置：以中心对称于右侧
        //右侧候选区中心 X = candidateX + candidateWidth/2
        //左侧对称点 X = radialCenterX - (candidateX + candidateWidth/2 - radialCenterX) = 2*radialCenterX - candidateX - candidateWidth/2
        int rightCenterX = candidateX + candidateWidth / 2;
        leftColumnX = 2 * radialCenterX - rightCenterX;

        //左侧清空按钮：与右侧移除区同高
        leftClearButtonY = removeZoneY;

        //底部索引控件（居中）
        indexNumberCenterX = this.width / 2;
        indexNumberY = this.height - 52;

        //左侧：切换轮盘索引快捷键说明（顶部，与候选区标题对称）
        //切换快捷键位置对称于候选区第一行
        //（纯文本渲染，在 render 中处理）

        //左侧：清空当前轮盘区域（底部，与右侧移除区对称，在 render 中自定义绘制）

        //底部：索引 - 按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("-"), btn -> adjustRadialIndex(-1))
                        .pos(indexNumberCenterX - 45, indexNumberY - 5)
                        .size(20, 20)
                        .build()
        );

        //底部：索引 + 按钮
        this.addRenderableWidget(
                Button.builder(Component.literal("+"), btn -> adjustRadialIndex(+1))
                        .pos(indexNumberCenterX + 25, indexNumberY - 5)
                        .size(20, 20)
                        .build()
        );

        //左下：保存并返回（80x20 与其他面板统一）
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.save_and_return"),
                                btn -> saveAndReturn())
                        .pos(10, this.height - 30)
                        .size(80, 20)
                        .build()
        );

        //左下：默认（80x20 与其他面板统一）
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.restore_defaults"),
                                btn -> restoreRadialDefaults())
                        .pos(100, this.height - 30)
                        .size(80, 20)
                        .build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //开启半透明混合
        RenderSystem.enableBlend();

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        //绘制标题
        graphics.drawCenteredString(font, this.title, this.width / 2, 10, 0xFFFFFF);

        //更新鼠标悬停描述
        updateHoverDescription(mouseX, mouseY);

        //绘制轮盘
        for (int i = 0; i < SLOT_COUNT; i++) {
            drawConfigSlot(graphics, i, mouseX, mouseY);
        }

        //绘制候选区边框和内容
        drawCandidateArea(graphics, mouseX, mouseY);

        //绘制移除区
        drawRemoveZone(graphics, mouseX, mouseY);

        //绘制左侧清空轮盘区域（与移除区风格一致）
        drawLeftClearZone(graphics, mouseX, mouseY);

        //左侧：切换轮盘索引快捷键说明（与候选区标题对称、位置对调后在上方）
        drawLeftSwitchShortcut(graphics);

        //绘制底部描述（上移，与索引间距增大）
        if (!hoverDescription.isEmpty()) {
            graphics.drawCenteredString(font, hoverDescription,
                    this.width / 2, indexNumberY - 26, 0xCCCCCC);
        }

        //绘制索引标签和数字（居中）
        Component indexLabel = Component.translatable("screen.stellarmod.tool_core.settings.radial_index");
        graphics.drawCenteredString(font, indexLabel, indexNumberCenterX, indexNumberY - 14, 0xAAAAAA);
        String indexStr = String.valueOf(currentConfigIndex);
        graphics.drawCenteredString(font, indexStr, indexNumberCenterX, indexNumberY, 0xFFFFFF);

        //校验警告：设置功能必须出现在某个轮盘上
        if (showSettingsWarning) {
            Component warning = Component.translatable(
                    "screen.stellarmod.tool_core.settings.settings_required_warning");
            graphics.drawCenteredString(font, warning, this.width / 2,
                    indexNumberY - 42, 0xFFFF4444);
        }

        //关闭半透明混合，因为半透明混合不会自动关闭，一直开启会导致渲染错误
        RenderSystem.disableBlend();
    }

    /**
     * 绘制左侧切换快捷键说明。
     * 与右侧候选区标题在 Y 方向上对称。
     */
    private void drawLeftSwitchShortcut(GuiGraphics graphics) {
        int labelY = candidateY - 10; //与候选区标题同一行
        Component shortcutLabel = Component.translatable(
                "screen.stellarmod.tool_core.settings.switch_shortcut");
        int labelWidth = font.width(shortcutLabel);
        graphics.drawString(font, shortcutLabel,
                leftColumnX - labelWidth / 2, labelY, 0xAAAAAA);

        //快捷键文字
        Component keyText = Component.literal("Shift");
        int keyWidth = font.width(keyText);
        graphics.drawString(font, keyText,
                leftColumnX - keyWidth / 2, labelY + 12, 0xFFFFFF);
    }

    /**
     * 绘制单个配置槽位。
     */
    private void drawConfigSlot(GuiGraphics graphics, int slotIndex, int mouseX, int mouseY) {
        //Math.toRadians把 角度（度） 转换成 弧度
        double angle = Math.toRadians(slotIndex * 45.0 - 90);
        int x = radialCenterX + (int) (RADIUS * Math.cos(angle)) - SLOT_SIZE / 2;
        int y = radialCenterY + (int) (RADIUS * Math.sin(angle)) - SLOT_SIZE / 2;

        boolean isHovered = isMouseInRect(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE);
        boolean isSelected = (slotIndex == selectedSlot);

        //背景色：选中 > 悬停 > 普通
        int bgColor;
        if (isSelected) {
            bgColor = 0xCCFFFF00; //黄色高亮选中
        } else if (isHovered) {
            bgColor = 0x80FFFFFF;
        } else {
            bgColor = 0x40000000;
        }
        //fill使用给定的坐标作为边界，用指定颜色填充矩形
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        //边框：选中时金色
        int borderColor = isSelected ? 0xFFFFFF00 : (isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
        graphics.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, borderColor);

        //绘制图标
        String actionId = RadialConfigStorage.getSlotAction(currentConfigIndex, slotIndex);
        ToolCoreRadialAction action = ToolCoreRadialAction.fromId(actionId);
        if (action != null && action.getIcon() != null) {
            //blit绘制纹理贴图的核心方法，简单说就是把一张图片（或图片的一部分）画到屏幕上指定位置
            graphics.blit(action.getIcon(), x + 4, y + 4, 0, 0, 16, 16, 16, 16);
        } else if (actionId != null && !actionId.isEmpty()) {
            //非枚举功能显示首字母
            String letter = actionId.substring(0, 1).toUpperCase();
            graphics.drawCenteredString(font, letter, x + SLOT_SIZE / 2, y + SLOT_SIZE / 2 - 4, 0xFFFFFF);
        }
    }

    /**
     * 绘制候选区（可用功能列表）。
     */
    private void drawCandidateArea(GuiGraphics graphics, int mouseX, int mouseY) {
        //边框
        graphics.renderOutline(candidateX, candidateY, candidateWidth, candidateHeight, 0xFF888888);
        //标题
        graphics.drawCenteredString(font,
                Component.translatable("screen.stellarmod.tool_core.settings.available_actions"),
                candidateX + candidateWidth / 2, candidateY - 10, 0xAAAAAA);

        //绘制每个可用功能图标
        for (int i = 0; i < availableActions.size(); i++) {
            int col = i % CANDIDATE_COLS;
            int row = i / CANDIDATE_COLS;
            int itemX = candidateX + 5 + col * CANDIDATE_ITEM_SIZE;
            int itemY = candidateY + 5 + row * CANDIDATE_ITEM_SIZE;

            ToolCoreRadialAction action = ToolCoreRadialAction.fromId(availableActions.get(i));
            boolean hovered = isMouseInRect(mouseX, mouseY, itemX, itemY, CANDIDATE_ITEM_SIZE - 2, CANDIDATE_ITEM_SIZE - 2);

            int bg = hovered ? 0x80FFFFFF : 0x40000000;
            graphics.fill(itemX, itemY, itemX + CANDIDATE_ITEM_SIZE - 2, itemY + CANDIDATE_ITEM_SIZE - 2, bg);
            graphics.renderOutline(itemX, itemY, CANDIDATE_ITEM_SIZE - 2, CANDIDATE_ITEM_SIZE - 2,
                    hovered ? 0xFFFFFFFF : 0xFF666666);

            if (action != null && action.getIcon() != null) {
                graphics.blit(action.getIcon(), itemX + 3, itemY + 3, 0, 0, 16, 16, 16, 16);
            } else {
                //理论不需要，候选区的 availableActions 来自 ToolCoreRadialAction.values()
                //具体来源：ToolCoreRadialAction action = ToolCoreRadialAction.fromId(availableActions.get(i));
                //全是枚举值，每个都有图标，但为了防止枚举处没有放入图标故而保留显示首字母
                String id = availableActions.get(i);
                String letter = id.substring(0, 1).toUpperCase();
                graphics.drawCenteredString(font, letter,
                        itemX + (CANDIDATE_ITEM_SIZE - 2) / 2,
                        itemY + (CANDIDATE_ITEM_SIZE - 2) / 2 - 4, 0xFFFFFF);
            }
        }
    }

    /**
     * 绘制移除区。
     */
    private void drawRemoveZone(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean hovered = isMouseInRect(mouseX, mouseY, removeZoneX, removeZoneY,
                REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT);
        int bg = hovered ? 0x80FF4444 : 0x40666666;
        int border = hovered ? 0xFFFF4444 : 0xFF888888;
        graphics.fill(removeZoneX, removeZoneY, removeZoneX + REMOVE_ZONE_WIDTH,
                removeZoneY + REMOVE_ZONE_HEIGHT, bg);
        graphics.renderOutline(removeZoneX, removeZoneY, REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT, border);
        graphics.drawCenteredString(font,
                Component.translatable("screen.stellarmod.tool_core.settings.remove_slot"),
                removeZoneX + REMOVE_ZONE_WIDTH / 2, removeZoneY + REMOVE_ZONE_HEIGHT / 2 - 4,
                hovered ? 0xFFFF4444 : 0xFFAAAAAA);
    }

    /**
     * 绘制左侧清空轮盘区域（与右侧移除区风格一致）。
     */
    private void drawLeftClearZone(GuiGraphics graphics, int mouseX, int mouseY) {
        int zoneX = leftColumnX - REMOVE_ZONE_WIDTH / 2;
        int zoneY = leftClearButtonY;
        boolean hovered = isMouseInRect(mouseX, mouseY, zoneX, zoneY,
                REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT);
        int bg = hovered ? 0x80FF4444 : 0x40666666;
        int border = hovered ? 0xFFFF4444 : 0xFF888888;
        graphics.fill(zoneX, zoneY, zoneX + REMOVE_ZONE_WIDTH,
                zoneY + REMOVE_ZONE_HEIGHT, bg);
        graphics.renderOutline(zoneX, zoneY, REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT, border);
        graphics.drawCenteredString(font,
                Component.translatable("screen.stellarmod.tool_core.settings.clear_radial"),
                zoneX + REMOVE_ZONE_WIDTH / 2, zoneY + REMOVE_ZONE_HEIGHT / 2 - 4,
                hovered ? 0xFFFF4444 : 0xFFAAAAAA);
    }

    /**
     * 更新底部悬停描述。
     */
    private void updateHoverDescription(int mouseX, int mouseY) {
        //检查是否悬停在轮盘槽位上
        for (int i = 0; i < SLOT_COUNT; i++) {
            double angle = Math.toRadians(i * 45.0 - 90);
            int x = radialCenterX + (int) (RADIUS * Math.cos(angle)) - SLOT_SIZE / 2;
            int y = radialCenterY + (int) (RADIUS * Math.sin(angle)) - SLOT_SIZE / 2;
            if (isMouseInRect(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
                String actionId = RadialConfigStorage.getSlotAction(currentConfigIndex, i);
                if (actionId != null) {
                    ToolCoreRadialAction action = ToolCoreRadialAction.fromId(actionId);
                    if (action != null) {
                        hoverDescription = Component.translatable("action.stellarmod." + action.getId()).getString();
                    } else {
                        hoverDescription = actionId;
                    }
                } else {
                    hoverDescription = Component.translatable(
                            "screen.stellarmod.tool_core.settings.empty_slot").getString();
                }
                return;
            }
        }

        //检查候选区功能图标
        for (int i = 0; i < availableActions.size(); i++) {
            int col = i % CANDIDATE_COLS;
            int row = i / CANDIDATE_COLS;
            int itemX = candidateX + 5 + col * CANDIDATE_ITEM_SIZE;
            int itemY = candidateY + 5 + row * CANDIDATE_ITEM_SIZE;
            if (isMouseInRect(mouseX, mouseY, itemX, itemY, CANDIDATE_ITEM_SIZE - 2, CANDIDATE_ITEM_SIZE - 2)) {
                ToolCoreRadialAction action = ToolCoreRadialAction.fromId(availableActions.get(i));
                if (action != null) {
                    hoverDescription = Component.translatable("action.stellarmod." + action.getId()).getString();
                }
                return;
            }
        }

        //检查移除区
        if (isMouseInRect(mouseX, mouseY, removeZoneX, removeZoneY, REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT)) {
            hoverDescription = Component.translatable(
                    "screen.stellarmod.tool_core.settings.remove_slot_desc").getString();
            return;
        }

        //默认描述
        if (selectedSlot >= 0) {
            hoverDescription = Component.translatable(
                    "screen.stellarmod.tool_core.settings.select_action_hint").getString();
        } else {
            hoverDescription = Component.translatable(
                    "screen.stellarmod.tool_core.settings.click_slot_hint").getString();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        //1. 检查是否点击轮盘槽位
        for (int i = 0; i < SLOT_COUNT; i++) {
            double angle = Math.toRadians(i * 45.0 - 90);
            int x = radialCenterX + (int) (RADIUS * Math.cos(angle)) - SLOT_SIZE / 2;
            int y = radialCenterY + (int) (RADIUS * Math.sin(angle)) - SLOT_SIZE / 2;
            if (isMouseInRect(mx, my, x, y, SLOT_SIZE, SLOT_SIZE)) {
                //点击之前被选中的地方取消选中
                if (selectedSlot == i) {
                    selectedSlot = -1;
                } else {
                    selectedSlot = i;
                }
                showSettingsWarning = false; //用户有操作，清除警告
                return true;
            }
        }

        //2. 检查是否点击候选区功能图标
        //选中槽位后点击候选区以设置功能
        if (selectedSlot >= 0) {
            for (int i = 0; i < availableActions.size(); i++) {
                int col = i % CANDIDATE_COLS;
                int row = i / CANDIDATE_COLS;
                int itemX = candidateX + 5 + col * CANDIDATE_ITEM_SIZE;
                int itemY = candidateY + 5 + row * CANDIDATE_ITEM_SIZE;
                if (isMouseInRect(mx, my, itemX, itemY, CANDIDATE_ITEM_SIZE - 2, CANDIDATE_ITEM_SIZE - 2)) {
                    RadialConfigStorage.setSlotAction(currentConfigIndex, selectedSlot,
                            availableActions.get(i));
                    selectedSlot = (selectedSlot + 1) % SLOT_COUNT;
                    showSettingsWarning = false;
                    return true;
                }
            }
        }

        //3. 检查是否点击左侧清空轮盘区域
        int leftZoneX = leftColumnX - REMOVE_ZONE_WIDTH / 2;
        if (isMouseInRect(mx, my, leftZoneX, leftClearButtonY,
                REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT)) {
            clearCurrentRadial();
            return true;
        }

        //4. 检查是否点击移除区
        if (selectedSlot >= 0 && isMouseInRect(mx, my, removeZoneX, removeZoneY,
                REMOVE_ZONE_WIDTH, REMOVE_ZONE_HEIGHT)) {
            RadialConfigStorage.removeSlotAction(currentConfigIndex, selectedSlot);
            selectedSlot = (selectedSlot + 1) % SLOT_COUNT;
            showSettingsWarning = false;
            return true;
        }

        //5. 点击空白处取消选中
        selectedSlot = -1;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * 调整轮盘索引（-1 或 +1）。
     */
    private void adjustRadialIndex(int delta) {
        int newIndex = currentConfigIndex + delta;
        if (newIndex < 1 || newIndex > RadialConfigStorage.MAX_RADIAL_INDEX) return;

        if (delta > 0 && !RadialConfigStorage.hasRadial(newIndex)) {
            //新建轮盘
            RadialConfigStorage.addRadial(newIndex);
        }

        currentConfigIndex = newIndex;
        selectedSlot = -1;
        showSettingsWarning = false;
    }

    /**
     * 清空当前轮盘所有槽位。
     */
    private void clearCurrentRadial() {
        RadialConfigStorage.clearRadial(currentConfigIndex);
        selectedSlot = -1;
    }

    /**
     * 保存配置并返回父屏幕。
     * 校验：设置功能必须出现在至少一个轮盘上，否则显示警告并阻止保存。
     */
    private void saveAndReturn() {
        if (!RadialConfigStorage.hasSettingsAssigned()) {
            showSettingsWarning = true;
            return;
        }
        doSave();
    }

    /**
     * 执行保存：持久化配置到文件，然后返回父屏幕（设置面板）。
     */
    private void doSave() {
        RadialConfigStorage.save();
        Minecraft.getInstance().setScreen(parent);
    }

    /**
     * 恢复当前轮盘的默认配置。
     */
    private void restoreRadialDefaults() {
        RadialConfigStorage.clearRadial(currentConfigIndex);
        selectedSlot = -1;
        showSettingsWarning = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        //关闭时校验：若设置未被分配到任何轮盘，重置为默认以防玩家锁死
        if (!RadialConfigStorage.hasSettingsAssigned()) {
            RadialConfigStorage.resetToDefault();
        } else {
            RadialConfigStorage.save();
        }
        Minecraft.getInstance().setScreen(parent);
    }

    /**
     * 判断鼠标坐标是否在指定矩形区域内。
     */
    private static boolean isMouseInRect(int mouseX, int mouseY, int rectX, int rectY, int rectW, int rectH) {
        return mouseX >= rectX && mouseX <= rectX + rectW
                && mouseY >= rectY && mouseY <= rectY + rectH;
    }
}

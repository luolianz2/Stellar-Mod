package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.SyncToolCoreMatrixSettingsPacket;
import com.luolian.stellarmod.server.data.toolcore.StellarMatrixRegistry;
import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 矩阵模块设置屏幕，用于管理工具核心上已附加的各个矩阵效果，
 * 包括开关效果、调节每个效果的生效等级。
 */
public class ToolCoreMatrixModuleScreen extends Screen {

    private final ItemStack toolStack;          // 实际的物品引用
    private final int slot;                     // 物品在玩家背包中的槽位
    private final List<StellarMatrixSettingEntry> entries = new ArrayList<>(); // 存储当前工具核心所拥有的所有矩阵效果信息
    private int leftTextX;  // 文字起始 X 坐标（左侧）
    private int contentY;   // 记录内容区域的起始 Y 坐标（垂直位置）
    private static final int ENTRY_HEIGHT = 24; // 每个矩阵效果条目的固定行高（像素）

    public ToolCoreMatrixModuleScreen(ItemStack toolStack) {
        super(Component.translatable("screen.stellarmod.tool_core.matrix"));
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        int foundSlot = player.getInventory().findSlotMatchingItem(toolStack);
        this.slot = foundSlot != -1 ? foundSlot : player.getInventory().selected;
        this.toolStack = player.getInventory().getItem(this.slot);
    }

    @Override
    protected void init() {
        super.init();
        // 布局：文字左对齐，按钮紧贴文字右侧，间隔约8个字符
        leftTextX = 20;
        contentY = 40;

        entries.clear();

        // 获取工具核心当前附加的所有矩阵效果
        Set<String> matrixIds = ToolCoreItem.getAttachedMatrixEffects(toolStack);
        for (String id : matrixIds) {
            StellarMatrixEffect effect = StellarMatrixRegistry.get(id);
            if (effect != null) {
                entries.add(new StellarMatrixSettingEntry(id, effect));
            }
        }

        // 为每个矩阵效果创建开关按钮、-、数字、+ 控件
        for (int i = 0; i < entries.size(); i++) {
            StellarMatrixSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT;

            // 计算文字宽度，按钮放置在文字右侧 8 个字符间距处
            Component displayName = entry.effect().getDisplayName();
            int textWidth = font.width(displayName);
            int gap = font.width("        ");
            int buttonX = leftTextX + textWidth + gap;

            // 获取当前最大等级和生效等级
            int maxLevel = ToolCoreItem.getMatrixTotalLevel(toolStack, entry.id());
            int activeLevel = ToolCoreItem.getMatrixActiveLevel(toolStack, entry.id());

            // 创建开关按钮，初始文字根据生效等级是否 > 0 决定
            boolean isEnabled = activeLevel > 0;
            Button toggleButton = Button.builder(
                            isEnabled ? Component.translatable("stellarmod.options.on")
                                    : Component.translatable("stellarmod.options.off"),
                            btn -> {
                                if (activeLevel > 0) {
                                    // 当前开启，点击关闭 -> 设为 0
                                    setActiveLevelWithSync(entry.id(), 0);
                                } else {
                                    // 当前关闭，点击开启 -> 恢复为最大等级
                                    setActiveLevelWithSync(entry.id(), maxLevel);
                                }
                            })
                    .pos(buttonX, y)
                    .size(50, 20)
                    .build();
            this.addRenderableWidget(toggleButton);

            // - 按钮：减小生效等级
            int minusX = buttonX + 55;
            Button minusBtn = Button.builder(Component.literal("-"), btn -> {
                int newLevel = Math.max(0, activeLevel - 1);
                setActiveLevelWithSync(entry.id(), newLevel);
            }).pos(minusX, y).size(20, 20).build();
            this.addRenderableWidget(minusBtn);

            // 等级显示（不可点击的数字）
            Component levelText = Component.literal(String.valueOf(activeLevel));
            int levelX = minusX + 25;
            Button levelLabel = Button.builder(levelText, btn -> {}).pos(levelX, y).size(20, 20).build();
            levelLabel.active = false; // 置灰，不允许点击
            this.addRenderableWidget(levelLabel);

            // + 按钮：增大生效等级
            int plusX = levelX + 25;
            Button plusBtn = Button.builder(Component.literal("+"), btn -> {
                int newLevel = Math.min(maxLevel, activeLevel + 1);
                setActiveLevelWithSync(entry.id(), newLevel);
            }).pos(plusX, y).size(20, 20).build();
            this.addRenderableWidget(plusBtn);
        }

        // 底部按钮：保存并退出、恢复默认
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.save_and_exit"), btn -> onClose())
                        .pos(10, this.height - 30)
                        .size(80, 20)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.restore_defaults"), btn -> restoreDefaults())
                        .pos(100, this.height - 30)
                        .size(80, 20)
                        .build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // 绘制标题
        graphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        // 绘制每个矩阵效果的名称（文字在左侧）
        for (int i = 0; i < entries.size(); i++) {
            StellarMatrixSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT + 5;
            graphics.drawString(font, entry.effect().getDisplayName(), leftTextX, y, 0xFFFFFF);
        }

        // 如果没有任何矩阵效果，显示提示
        if (entries.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.stellarmod.tool_core.matrix.empty"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }

        // 鼠标悬停在矩阵效果名称上时显示详细描述、作者吐槽和等级信息
        for (int i = 0; i < entries.size(); i++) {
            StellarMatrixSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT + 5;
            Component name = entry.effect().getDisplayName();
            int nameWidth = font.width(name);
            if (mouseX >= leftTextX && mouseX <= leftTextX + nameWidth && mouseY >= y && mouseY <= y + font.lineHeight) {
                // 构建 Tooltip 内容：描述行 + 作者吐槽 + 最大等级 + 累计等级
                List<Component> tooltipLines = new ArrayList<>();
                List<Component> descList = entry.effect().getDescription();
                if (descList != null) {
                    tooltipLines.addAll(descList);
                }
                Component note = entry.effect().getAuthorNote();
                if (note != null) {
                    tooltipLines.add(note);
                }
                int totalLevel = ToolCoreItem.getMatrixTotalLevel(toolStack, entry.id());
                int hardMax = entry.effect().getMaxLevel();
                tooltipLines.add(Component.literal("累计等级：Lv." + totalLevel + " / 上限：Lv." + hardMax));
                graphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
                break; // 一次只显示一个
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 不暂停游戏
    }

    // ========== 等级调节与同步 ==========

    /**
     * 设置某个矩阵效果的生效等级，保存到 NBT，触发同步，并刷新界面。
     */
    private void setActiveLevelWithSync(String effectId, int level) {
        ToolCoreItem.setMatrixActiveLevel(toolStack, effectId, level);
        syncMatrixSettings();
        rebuildWidgets();
    }

    /**
     * 将当前的矩阵设置和生效等级同步到服务端。
     * 使用构造时记录的槽位，确保同步的是正确的物品。
     */
    private void syncMatrixSettings() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CompoundTag settings = toolStack.getOrCreateTag().getCompound(ToolCoreItem.TAG_MATRIX_SETTINGS);
        CompoundTag activeLevels = toolStack.getOrCreateTag().getCompound(ToolCoreItem.TAG_MATRIX_ACTIVE_LEVELS);
        StellarNetworkHandler.INSTANCE.sendToServer(new SyncToolCoreMatrixSettingsPacket(this.slot, settings, activeLevels));
    }

    /**
     * 恢复默认设置：移除所有矩阵效果的开关记录和生效等级限制，
     * 使它们回到默认的开启状态和最大等级。
     */
    private void restoreDefaults() {
        toolStack.getOrCreateTag().remove(ToolCoreItem.TAG_MATRIX_SETTINGS);
        toolStack.getOrCreateTag().remove(ToolCoreItem.TAG_MATRIX_ACTIVE_LEVELS);
        // 不删除 TAG_MATRIX_LEVELS 和 TAG_MATRIX_ORDER（累计等级和顺序属于物品固有数据）
        syncMatrixSettings();
        this.entries.clear();
        rebuildWidgets();
    }

    /**
     * 内部记录：矩阵效果 ID 及其效果实例。
     */
    private record StellarMatrixSettingEntry(String id, StellarMatrixEffect effect) {}
}

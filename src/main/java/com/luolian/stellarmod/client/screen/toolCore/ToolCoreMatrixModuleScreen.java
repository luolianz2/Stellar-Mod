package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.SyncToolCoreMatrixSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 矩阵模块设置屏幕，用于管理工具核心的矩阵效果（目前仅包含总开关）。
 */
public class ToolCoreMatrixModuleScreen extends Screen {

    private final ItemStack toolStack;          //实际的物品引用
    private final int slot;                     //物品在玩家背包中的槽位
    private static final String TAG_MATRIX_SETTINGS = "ToolCoreMatrixSettings";

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
        int leftTextX = 20;
        int y = 40;

        // 获取当前矩阵模块总开关状态
        boolean isEnabled = isMatrixEnabled(toolStack);

        // 计算文字宽度，按钮放置在文字右侧 8 个字符间距处
        Component label = Component.translatable("screen.stellarmod.tool_core.matrix.enabled");
        int textWidth = font.width(label);
        int gap = font.width("        "); // 8 个空格的像素宽度
        int buttonX = leftTextX + textWidth + gap;

        // 总开关按钮，位于文字右侧
        Button toggleButton = Button.builder(
                        isEnabled ? Component.translatable("stellarmod.options.on")
                                : Component.translatable("stellarmod.options.off"),
                        btn -> {
                            boolean newState = !isMatrixEnabled(toolStack);
                            setMatrixEnabled(toolStack, newState);
                            btn.setMessage(newState ? Component.translatable("stellarmod.options.on")
                                    : Component.translatable("stellarmod.options.off"));
                        })
                .pos(buttonX, y)
                .size(50, 20)
                .build();
        this.addRenderableWidget(toggleButton);

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

        graphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        int leftTextX = 20;
        int y = 45;
        // 绘制“矩阵模块”标签，位于左侧，与右侧按钮对应
        graphics.drawString(font, Component.translatable("screen.stellarmod.tool_core.matrix.enabled"), leftTextX, y, 0xFFFFFF);

        // 预留效果设置区域（暂无内容）
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ========== NBT 读写 ==========

    public static boolean isMatrixEnabled(ItemStack stack) {
        CompoundTag root = stack.getOrCreateTag();
        if (root.contains(TAG_MATRIX_SETTINGS, Tag.TAG_COMPOUND)) {
            CompoundTag settings = root.getCompound(TAG_MATRIX_SETTINGS);
            if (settings.contains("enabled", Tag.TAG_BYTE)) {
                return settings.getBoolean("enabled");
            }
        }
        return true; // 默认开启
    }

    private void setMatrixEnabled(ItemStack stack, boolean enabled) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag settings = root.getCompound(TAG_MATRIX_SETTINGS);
        settings.putBoolean("enabled", enabled);
        root.put(TAG_MATRIX_SETTINGS, settings);
        syncSettings();
    }

    private void syncSettings() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        CompoundTag settings = toolStack.getOrCreateTag().getCompound(TAG_MATRIX_SETTINGS);
        StellarNetworkHandler.INSTANCE.sendToServer(new SyncToolCoreMatrixSettingsPacket(this.slot, settings));
    }

    private void restoreDefaults() {
        toolStack.getOrCreateTag().remove(TAG_MATRIX_SETTINGS);
        syncSettings();
        this.clearWidgets();
        this.init();
    }
}
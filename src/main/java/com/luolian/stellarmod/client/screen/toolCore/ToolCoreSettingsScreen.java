package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.client.config.RadialConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一设置面板，用于管理工具核心的各类非副词条/矩阵的功能设置。
 * 当前包含：轮盘槽位功能调整。
 * 布局风格与副词条/矩阵面板保持一致（左文字、右按钮）。
 */
public class ToolCoreSettingsScreen extends Screen {

    private final ItemStack toolStack;
    private static final int LEFT_TEXT_X = 20;
    private static final int CONTENT_Y = 40;

    public ToolCoreSettingsScreen(ItemStack toolStack) {
        //传入父级的this.title里
        super(Component.translatable("screen.stellarmod.tool_core.settings_general"));

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        int foundSlot = player.getInventory().findSlotMatchingItem(toolStack);
        int slot = foundSlot != -1 ? foundSlot : player.getInventory().selected;
        this.toolStack = player.getInventory().getItem(slot);
    }

    @Override
    protected void init() {
        super.init();
        //设置条目列表

        //条目 1: 轮盘槽位功能调整
        String labelKey = "screen.stellarmod.tool_core.settings.radial_slot_config";
        Component label = Component.translatable(labelKey);
        int labelWidth = font.width(label);
        int gap = font.width("        "); //8个空格
        int buttonX = LEFT_TEXT_X + labelWidth + gap;
        int y = CONTENT_Y;

        Button configButton = Button.builder(
                        Component.translatable("screen.stellarmod.tool_core.settings.configure"),
                        btn -> {
                            //打开轮盘槽位配置子面板
                            Minecraft.getInstance().setScreen(
                                    new ToolCoreRadialSlotConfigScreen(toolStack, this)
                            );
                        })
                .pos(buttonX, y)
                .size(50, 20)
                .build();
        this.addRenderableWidget(configButton);

        //左下角按钮：保存并退出
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.save_and_exit"),
                                btn -> onClose())
                        .pos(10, this.height - 30)
                        .size(80, 20)
                        .build()
        );

        //左下角按钮：恢复默认
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.restore_defaults"),
                                btn -> restoreDefaults())
                        .pos(100, this.height - 30)
                        .size(80, 20)
                        .build()
        );
    }

    @Override
    //@NotNull声明 变量不能为 null
    //partialTick即部分刻进度是 Minecraft 渲染系统中重要的参数，其告诉渲染器当前帧的时间点，处于上下两个游戏 Tick 之间的什么位置，取值范围通常是 0.0f ~ 1.0f
    //用于平滑动画，避免每帧渲染同样的画面
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        //绘制标题
        //this.title在构造方法中已传至父级，font和width为默认设置（mc的）
        graphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        //绘制条目名称
        Component label = Component.translatable("screen.stellarmod.tool_core.settings.radial_slot_config");
        graphics.drawString(font, label, LEFT_TEXT_X, CONTENT_Y + 5, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 恢复默认设置：重置轮盘配置为默认。
     */
    private void restoreDefaults() {
        RadialConfigStorage.resetToDefault();
        //刷新界面
        rebuildWidgets();
    }
}

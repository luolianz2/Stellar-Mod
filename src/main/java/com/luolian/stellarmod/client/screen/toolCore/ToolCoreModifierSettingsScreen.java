package com.luolian.stellarmod.client.screen.toolCore;

import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import com.luolian.stellarmod.network.StellarNetworkHandler;
import com.luolian.stellarmod.network.toolcore.SyncToolCoreModifierSettingsPacket;
import com.luolian.stellarmod.server.data.toolcore.StellarModifierRegistry;
import com.luolian.stellarmod.server.data.toolcore.Material;
import com.luolian.stellarmod.server.item.custom.toolcore.ToolCoreItem;
import com.luolian.stellarmod.server.item.custom.toolcore.ToolCoreNBT;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 副词条设置屏幕，用于开关工具核心上的各个副词条效果，并调节每个副词条的生效等级。
 */
public class ToolCoreModifierSettingsScreen extends Screen {

    private final ItemStack toolStack;          //实际的物品引用
    private final int slot;                     //物品在玩家背包中的槽位
    private final List<StellarModifierSettingEntry> entries = new ArrayList<>(); //存储当前工具核心所拥有的所有副词条信息
    private int leftTextX;  //文字起始 X 坐标（左侧）
    private int contentY;   //记录内容区域的起始 Y 坐标（垂直位置）
    private static final int ENTRY_HEIGHT = 24; //定义每个副词条条目（包括开关按钮和名称）所占的固定高度（单位：像素）

    //TAG_MODIFIER_SETTINGS 已迁移至 ToolCoreNBT 统一管理

    public ToolCoreModifierSettingsScreen(ItemStack toolStack) {
        super(Component.translatable("screen.stellarmod.tool_core.settings"));
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        //找到物品在玩家背包中的实际槽位
        int foundSlot = player.getInventory().findSlotMatchingItem(toolStack);
        //如果找到了（条件为 true），则使用 foundSlot 作为 this.slot 的值。
        //如果没找到（条件为 false），则使用玩家当前手持的快捷栏槽位索引（selected）作为备用值。
        this.slot = foundSlot != -1 ? foundSlot : player.getInventory().selected;
        //通过槽位获取确切的物品引用，确保后续操作直接作用于背包中的真实物品
        this.toolStack = player.getInventory().getItem(this.slot);
    }

    @Override
    protected void init() {
        super.init();
        //布局：文字左对齐，按钮紧贴文字右侧，间隔约8个字符
        leftTextX = 20;
        contentY = 40;

        entries.clear();

        //获取工具核心当前拥有的所有副词条
        List<Material.StellarModifierEntry> modifiers = ToolCoreNBT.getAllModifiers(toolStack);
        for (Material.StellarModifierEntry entry : modifiers) {
            StellarModifierEffect effect = StellarModifierRegistry.get(entry.id());
            if (effect != null) {
                //所有配置参数已改为在效果类内部硬编码，不再通过 JSON 的 config 字段读取
                entries.add(new StellarModifierSettingEntry(entry.id(), effect));
            }
        }

        //先计算所有条目中最长名字的宽度，确保按钮列对齐
        int maxTextWidth = 0;
        for (StellarModifierSettingEntry entry : entries) {
            maxTextWidth = Math.max(maxTextWidth, font.width(entry.effect().getDisplayName()));
        }
        int gap = font.width("        "); //8 个空格字符的像素宽度
        int buttonX = leftTextX + maxTextWidth + gap;

        //为每个副词条创建开关按钮、-、数字、+ 控件
        for (int i = 0; i < entries.size(); i++) {
            StellarModifierSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT;

            //获取当前最大等级和生效等级
            int maxLevel = ToolCoreNBT.getModifierLevel(toolStack, entry.id());
            int activeLevel = ToolCoreNBT.getModifierActiveLevel(toolStack, entry.id());

            //创建开关按钮，初始文字根据生效等级是否 > 0 决定
            boolean isEnabled = activeLevel > 0;
            Button toggleButton = Button.builder(
                            isEnabled ? Component.translatable("stellarmod.options.on")
                                    : Component.translatable("stellarmod.options.off"),
                            btn -> {
                                if (activeLevel > 0) {
                                    //当前开启，点击关闭 -> 设为0
                                    setActiveLevelWithSync(entry.id(), 0);
                                } else {
                                    //当前关闭，点击开启 -> 恢复为最大等级
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

            //等级显示（不可点击的数字）
            Component levelText = Component.literal(String.valueOf(activeLevel));
            int levelX = minusX + 25;
            Button levelLabel = Button.builder(levelText, btn -> {}).pos(levelX, y).size(20, 20).build();
            levelLabel.active = false; //置灰，不允许点击
            this.addRenderableWidget(levelLabel);

            // + 按钮：增大生效等级
            int plusX = levelX + 25;
            Button plusBtn = Button.builder(Component.literal("+"), btn -> {
                int newLevel = Math.min(maxLevel, activeLevel + 1);
                setActiveLevelWithSync(entry.id(), newLevel);
            }).pos(plusX, y).size(20, 20).build();
            this.addRenderableWidget(plusBtn);
        }

        //添加左下角按钮：保存并退出（这里仅关闭界面，保存自动保存）
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.stellarmod.tool_core.settings.save_and_exit"), btn -> onClose())
                        .pos(10, this.height - 30)
                        .size(80, 20)
                        .build()
        );

        //添加左下角按钮：恢复默认（移除所有设置，恢复默认开启状态）
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

        //绘制标题
        graphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        //绘制每个副词条的名称（文字在左侧）
        for (int i = 0; i < entries.size(); i++) {
            StellarModifierSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT + 5;
            graphics.drawString(font, entry.effect().getDisplayName(), leftTextX, y, 0xFFFFFF);
        }

        //如果没有任何副词条，显示提示
        if (entries.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.stellarmod.tool_core.settings.empty"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }

        //鼠标悬停在副词条名称上时显示详细描述、作者吐槽和最大等级信息
        for (int i = 0; i < entries.size(); i++) {
            StellarModifierSettingEntry entry = entries.get(i);
            int y = contentY + i * ENTRY_HEIGHT + 5;
            //计算名称文本的宽度和范围（基于左侧文字实际位置）
            Component name = entry.effect().getDisplayName();
            int nameWidth = font.width(name);
            if (mouseX >= leftTextX && mouseX <= leftTextX + nameWidth && mouseY >= y && mouseY <= y + font.lineHeight) {
                //构建 Tooltip 内容：描述行 + 作者吐槽 + 最大等级
                List<Component> tooltipLines = new ArrayList<>(entry.effect().getDescription());
                tooltipLines.add(entry.effect().getAuthorNote());
                int maxLevel = ToolCoreNBT.getModifierLevel(toolStack, entry.id());
                tooltipLines.add(Component.literal("最大等级：Lv." + maxLevel));
                graphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
                break; //一次只显示一个
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; //不暂停游戏
    }

    //isModifierEnabled / setModifierEnabledDirect 已迁移至 ToolCoreNBT

    /**
     * 设置某个副词条的生效等级，保存到 NBT，触发同步，并刷新界面。
     */
    private void setActiveLevelWithSync(String id, int level) {
        ToolCoreNBT.setModifierActiveLevel(toolStack, id, level);
        syncModifierSettings();
        rebuildWidgets();
    }

    /**
     * 重新构建界面控件。
     */
    private void refreshWidgets() {
        this.clearWidgets();
        this.entries.clear();
        this.init();
    }

    /**
     * 将当前的副词条设置和生效等级同步到服务端。
     * 使用构造时记录的槽位，确保同步的是正确的物品。
     */
    private void syncModifierSettings() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CompoundTag settings = toolStack.getOrCreateTag().getCompound(ToolCoreNBT.TAG_MODIFIER_SETTINGS);
        CompoundTag activeLevels = toolStack.getOrCreateTag().getCompound(ToolCoreNBT.TAG_MODIFIER_ACTIVE_LEVELS);
        StellarNetworkHandler.INSTANCE.sendToServer(new SyncToolCoreModifierSettingsPacket(this.slot, settings, activeLevels));
    }

    /**
     * 恢复默认设置：移除所有副词条的开关记录和生效等级限制，使它们回到默认的开启状态和最大等级。
     */
    private void restoreDefaults() {
        //移除 NBT 中的设置标签
        toolStack.getOrCreateTag().remove(ToolCoreNBT.TAG_MODIFIER_SETTINGS);
        //移除生效等级限制
        toolStack.getOrCreateTag().remove(ToolCoreNBT.TAG_MODIFIER_ACTIVE_LEVELS);
        //同步到服务端
        syncModifierSettings();

        //重新初始化界面，刷新所有开关按钮的状态

        //移除当前屏幕上的所有可渲染控件（按钮、开关等）
        //如果不移除旧控件，直接重新初始化会导致新旧控件重叠显示，或者旧控件仍然响应用户点击
        //清空控件列表后，屏幕处于“空白”状态，等待重新添加
        this.clearWidgets();

        //清空副词条列表内容
        this.entries.clear();

        //重新调用初始化方法
        this.init();
    }

    /**
     * 内部记录：副词条 ID 及其效果实例。
     */
    private record StellarModifierSettingEntry(String id, StellarModifierEffect effect) {}
}
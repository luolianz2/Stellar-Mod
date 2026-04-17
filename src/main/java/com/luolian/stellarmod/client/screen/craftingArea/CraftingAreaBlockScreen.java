package com.luolian.stellarmod.client.screen.craftingArea;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CraftingAreaBlockScreen extends AbstractContainerScreen<CraftingAreaBlockMenu> {
    private static final ResourceLocation TEXTURE =
            StellarMod.location("textures/gui/crafting_area_block_gui.png");

    public CraftingAreaBlockScreen(CraftingAreaBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        //隐藏原版的玩家物品栏标题和容器标题，因为我们使用自定义纹理中的标题
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        //使用 GuiGraphics.blit 直接绘制背景纹理，该方法内部会自动处理着色器和纹理绑定，
        //无需手动调用 RenderSystem，可避免与其他模组的渲染状态冲突。
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        //渲染背景（包括暗色遮罩）
        renderBackground(guiGraphics);
        //调用父类渲染槽位和物品
        super.render(guiGraphics, mouseX, mouseY, delta);
        //渲染工具提示（必须最后调用，使其显示在最上层）
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
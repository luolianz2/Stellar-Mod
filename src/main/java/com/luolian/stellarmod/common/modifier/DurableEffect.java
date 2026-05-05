package com.luolian.stellarmod.common.modifier;

import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 耐用副词条效果。每级提供 8% 概率在挖掘/攻击时不消耗工具耐久。
 */
public class DurableEffect implements StellarModifierEffect {

    /** 每级提供的耐久减免概率 */
    private static final float CHANCE_PER_LEVEL = 0.08f;

    @Override
    public String getId() {
        return "stellarmod:durable";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("modifier.stellarmod_item.tool_core.durable.name")
                .withStyle(ChatFormatting.AQUA);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("modifier.stellarmod_item.tool_core.durable.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        return Component.translatable("modifier.stellarmod_item.tool_core.durable.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    /**
     * 每级 8% 概率跳过耐久消耗。
     *
     * @param modifierLevel 当前副词条生效等级
     * @return true 表示本次不消耗耐久
     */
    @Override
    public boolean shouldSkipDurability(int modifierLevel) {
        if (modifierLevel <= 0) return false;
        //Math.random()随机数范围 [0.0, 1.0)
        return Math.random() < modifierLevel * CHANCE_PER_LEVEL;
    }
}

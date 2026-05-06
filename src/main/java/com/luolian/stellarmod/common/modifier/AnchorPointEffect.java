package com.luolian.stellarmod.common.modifier;

import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AnchorPointEffect implements StellarModifierEffect {
    @Override
    public String getId() {
        return "stellarmod:anchor_point";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("modifier.stellarmod_item.tool_core.anchor_point.name")
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("modifier.stellarmod_item.tool_core.anchor_point.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        return Component.translatable("modifier.stellarmod_item.tool_core.anchor_point.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}

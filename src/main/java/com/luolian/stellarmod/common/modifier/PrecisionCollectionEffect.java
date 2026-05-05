package com.luolian.stellarmod.common.modifier;

import com.luolian.stellarmod.api.toolcore.StellarModifierEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PrecisionCollectionEffect implements StellarModifierEffect {
    @Override
    public String getId() {
        return "stellarmod:precision_collection";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("modifier.stellarmod_item.tool_core.precision_collection.name")
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("modifier.stellarmod_item.tool_core.precision_collection.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        return Component.translatable("modifier.stellarmod_item.tool_core.precision_collection.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

}

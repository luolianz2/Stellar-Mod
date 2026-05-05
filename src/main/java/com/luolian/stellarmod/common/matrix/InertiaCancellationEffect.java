package com.luolian.stellarmod.common.matrix;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class InertiaCancellationEffect implements StellarMatrixEffect {

    @Override public String getId() { return "stellarmod:inertia_cancellation"; }

    @Override public Component getDisplayName() {
        return Component.translatable("matrix.stellarmod_item.tool_core.inertia_cancellation.name").withStyle(ChatFormatting.GOLD);
    }

    @Override public List<Component> getDescription() {
        return List.of(Component.translatable("matrix.stellarmod_item.tool_core.inertia_cancellation.desc"));
    }

    @Override public Component getAuthorNote() {
        return Component.translatable("matrix.stellarmod_item.tool_core.inertia_cancellation.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    @Override public int getMaxLevel() { return 1; }

    @Override
    public void onPlayerTick(Player player, int activeLevel) {
        //惯性消除已由 PlayerTravelMixin 在客户端 travel() 尾部实现，
        //服务端 setDeltaMovement 对客户端预测的飞行移动无实际影响，故此处不再处理。
    }
}
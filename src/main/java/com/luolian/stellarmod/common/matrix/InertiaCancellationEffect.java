package com.luolian.stellarmod.common.matrix;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
        if (activeLevel <= 0) return;

        if (!player.level().isClientSide) {
            //仅在玩家主动飞行且无水平输入时取消惯性
            //玩家当前处于飞行状态+玩家左/右和前进/后退的输入值为0
            if (player.getAbilities().flying && player.xxa == 0.0F && player.zza == 0.0F) {
                //将速度向量设为0，即立即停止所有惯性移动，实现“急停”效果
                player.setDeltaMovement(Vec3.ZERO);
            }
        }
    }
}
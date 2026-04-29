package com.luolian.stellarmod.common.matrix;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 创造飞行矩阵效果。允许玩家像创造模式一样自由飞行，并根据等级提高飞行速度。
 * 最高等级：10级。
 */
public class CreativeFlightEffect implements StellarMatrixEffect {

    @Override
    public String getId() {
        return "stellarmod:creative_flight";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.name")
                .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.desc")
        );
    }

    @Override
    public Component getAuthorNote() {
        return Component.translatable("matrix.stellarmod_item.tool_core.creative_flight.author_note")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public void onPlayerTick(Player player, int activeLevel) {
        if (activeLevel <= 0) return;

        //启用飞行能力
        //mayfly = true 允许玩家飞行（如同处于创造模式）
        //flying = true 让玩家当前处于飞行状态，不会掉落
        //onUpdateAbilities() 将更改同步到客户端，确保界面和实际操作保持同步。
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();

        //调整水平飞行速度：基础 0.05，每级增加 0.01
        float horizontalSpeed = 0.05f + activeLevel * 0.01f;
        player.getAbilities().setFlyingSpeed(horizontalSpeed);

        //垂直飞行速度的调整由专门的 Mixin (PlayerTravelMixin) 处理，
        //以保证水平与垂直速度的独立控制和更好的兼容性。
    }
}
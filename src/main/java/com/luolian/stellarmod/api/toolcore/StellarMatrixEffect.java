package com.luolian.stellarmod.api.toolcore;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 矩阵效果接口，定义矩阵效果的通用行为。
 */
public interface StellarMatrixEffect {

    /** 效果唯一标识符，如 "stellarmod:creative_flight" */
    String getId();

    //获取显示名称（含颜色格式）
    Component getDisplayName();

    //获取效果描述（用于Ctrl显示）
    List<Component> getDescription();

    //获取作者吐槽（用于Ctrl显示）
    Component getAuthorNote();

    /** 该效果的最大可升级等级 */
    int getMaxLevel();

    /** 玩家每 tick 触发，由外部事件调用。activeLevel 为当前生效等级（由玩家设置）。 */
    default void onPlayerTick(Player player, int activeLevel) {}
}
package com.luolian.stellarmod.server.data.toolCore;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 材料属性定义
 * @param id             材料文件自身的标识（用于调试/日志）
 * @param itemId         对应的物品注册名，作为查询键
 * @param miningLevel    挖掘等级
 * @param miningSpeed    挖掘速度加成
 * @param attackDamage   攻击伤害加成
 * @param durability     首次添加提供的最大耐久值
 * @param upgradeCost    首次升级所需数量
 * @param modifiers      副词条列表
 */
public record Material(
        ResourceLocation id,
        ResourceLocation itemId,
        int miningLevel,
        float miningSpeed,
        float attackDamage,
        int durability,
        int upgradeCost,
        List<StellarModifierEntry> modifiers
) {
    public record StellarModifierEntry(
            String id,                 //效果唯一标识，如 "stellarmod:electromagnetic"
            JsonObject config          //可选的配置参数
    ) {}
}
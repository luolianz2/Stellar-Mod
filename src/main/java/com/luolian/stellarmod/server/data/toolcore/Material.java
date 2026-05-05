package com.luolian.stellarmod.server.data.toolcore;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 材料属性定义
 * @param id             材料文件自身的标识（用于调试/日志）
 * @param itemId         对应的物品注册名，作为查询键
 * @param miningLevel    挖掘等级
 * @param miningSpeed    挖掘速度加成
 * @param attackDamage   攻击伤害加成
 * @param durability     耐久加成
 * @param upgradeCost    首次升级所需消耗的数量
 * @param aliases        某个别名相关的材料列表
 * @param modifiers      副词条列表（每个条目包含效果ID和初始等级）
 */
public record Material(
        ResourceLocation id,
        ResourceLocation itemId,
        int miningLevel,
        float miningSpeed,
        float attackDamage,
        int durability,
        int upgradeCost,
        List<ResourceLocation> aliases,
        List<StellarModifierEntry> modifiers
) {
    /**
     * 副词条条目
     * @param id   效果唯一标识，如 "stellarmod:electromagnetic"
     * @param level 该材料提供的该副词条初始等级（正数，默认1）
     */
    public record StellarModifierEntry(String id, int level) {
        public StellarModifierEntry(String id) {
            this(id, 1);
        }
    }
}
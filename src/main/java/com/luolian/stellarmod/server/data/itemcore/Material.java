package com.luolian.stellarmod.server.data.itemcore;

import net.minecraft.resources.ResourceLocation;

/**
 * 材料属性定义
 * @param id             材料文件自身的标识（用于调试/日志）
 * @param itemId         对应的物品注册名，作为查询键
 * @param miningLevel    挖掘等级
 * @param miningSpeed    挖掘速度加成
 * @param attackDamage   攻击伤害加成
 * @param durability     耐久加成
 * @param enchantAbility 附魔能力加成
 * @param color          渲染颜色（0xRRGGBB）
 */
public record Material(
        ResourceLocation id,
        ResourceLocation itemId,
        int miningLevel,
        float miningSpeed,
        float attackDamage,
        int durability,
        int enchantAbility,
        int color
) {}
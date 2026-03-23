package com.luolian.stellarmod.server.data.baseplate;

import net.minecraft.resources.ResourceLocation;

//材料类，存储固定属性
public record Material(
        ResourceLocation id,          //物品注册名，如"examplemod:iron"
        int miningLevel,              //挖掘等级（数字）
        float miningSpeed,            //挖掘速度
        float attackDamage,           //攻击伤害
        int durability,               //耐久值
        int enchantability,           //附魔能力
        int color                     //用于后续渲染的颜色值（0xRRGGBB）
) {}
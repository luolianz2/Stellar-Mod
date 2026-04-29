package com.luolian.stellarmod.server.item.custom;

import net.minecraft.world.item.Item;

/**
 * 矩阵升级物品，携带特定的矩阵效果及其初始等级。
 * 该物品只能放入组装台的 6 号槽位，用于为工具核心附加矩阵效果。
 */
public class StellarMatrixItem extends Item {

    private final String effectId;   //矩阵效果唯一标识符，例如 "stellarmod:creative_flight"
    private final int level;         //该物品提供的效果初始等级

    /**
     * @param properties 物品基本属性（由注册时提供）
     * @param effectId   矩阵效果的唯一 ID
     * @param level      该物品提供的效果等级（正数）
     */
    public StellarMatrixItem(Properties properties, String effectId, int level) {
        super(properties);
        this.effectId = effectId;
        this.level = level;
    }

    /** 获取该矩阵物品携带的效果 ID */
    public String getEffectId() {
        return effectId;
    }

    /** 获取该矩阵物品携带的效果初始等级 */
    public int getLevel() {
        return level;
    }
}
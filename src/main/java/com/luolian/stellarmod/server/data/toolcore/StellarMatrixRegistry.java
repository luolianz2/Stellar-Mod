package com.luolian.stellarmod.server.data.toolcore;

import com.luolian.stellarmod.api.toolcore.StellarMatrixEffect;
import com.luolian.stellarmod.common.matrix.CreativeFlightEffect;
import com.luolian.stellarmod.common.matrix.InertiaCancellationEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * 矩阵效果注册表，用于根据效果 ID 获取对应的效果实例。
 */
public class StellarMatrixRegistry {
    private static final Map<String, StellarMatrixEffect> EFFECTS = new HashMap<>();

    /**
     * 注册一个矩阵效果。
     */
    public static void register(StellarMatrixEffect effect) {
        EFFECTS.put(effect.getId(), effect);
    }

    /**
     * 根据 ID 获取矩阵效果，若未注册则返回 null。
     */
    public static StellarMatrixEffect get(String id) {
        return EFFECTS.get(id);
    }

    /**
     * 在模组初始化时调用，注册所有内置矩阵效果。
     */
    public static void registerAll() {
        register(new CreativeFlightEffect());
        register(new InertiaCancellationEffect());
        //未来新增效果只需在此添加一行
    }
}
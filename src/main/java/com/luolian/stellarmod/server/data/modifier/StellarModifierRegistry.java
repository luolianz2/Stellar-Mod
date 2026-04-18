package com.luolian.stellarmod.server.data.modifier;

import com.luolian.stellarmod.api.modifier.StellarModifierEffect;
import com.luolian.stellarmod.common.modifier.ElectromagneticEffect;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//效果注册表
public class StellarModifierRegistry {
    private static final Map<String, StellarModifierEffect> EFFECTS = new HashMap<>();

    public static void register(StellarModifierEffect effect) {
        EFFECTS.put(effect.getId(), effect);
    }

    @Nullable
    public static StellarModifierEffect get(String id) {
        return EFFECTS.get(id);
    }

    public static Collection<StellarModifierEffect> getAll() {
        return EFFECTS.values();
    }

    /**
     * 集中注册所有内置副词条效果。
     * 在模组初始化时调用此方法
     */
    public static void registerAll() {
        register(new ElectromagneticEffect());
        //未来新增效果只需在此添加一行
    }
}

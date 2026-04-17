package com.luolian.stellarmod.server.data.itemcore;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaterialManager {
    //核心映射：物品 ID -> Material
    private static final Map<ResourceLocation, Material> BY_ITEM = new HashMap<>();
    //默认材料（空气物品，属性全为0/基础值）
    private static final Material DEFAULT = new Material(
            ResourceLocation.fromNamespaceAndPath("stellarmod", "default"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "air"),
            0, 1.0f, 0.0f, 0, 0, 0xFFFFFF, 1
    );

    public static void clear() {
        BY_ITEM.clear();
    }

    /**
     * 注册一个材料，如果同一物品 ID 已有材料，后者会覆盖前者
     */
    public static void register(Material material) {
        BY_ITEM.put(material.itemId(), material);
    }

    /**
     * 根据物品注册名获取对应的材料属性，若未注册则返回默认材料
     */
    public static Material getMaterial(ResourceLocation itemId) {
        return BY_ITEM.getOrDefault(itemId, DEFAULT);
    }

    /**
     * 获取所有已加载的材料（不包含默认材料）
     */
    public static Collection<Material> getAllMaterials() {
        return BY_ITEM.values();
    }

    //检查某个物品是否已注册材料
    public static boolean hasMaterial(ResourceLocation itemId) {
        return BY_ITEM.containsKey(itemId);
    }
}
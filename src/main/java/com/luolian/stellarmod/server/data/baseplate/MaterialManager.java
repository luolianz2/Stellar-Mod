package com.luolian.stellarmod.server.data.baseplate;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaterialManager {
    private static final Map<ResourceLocation,Material> MATERIALS = new HashMap<>();
    private static final Material DEFAULT = new Material(   //默认值
            ResourceLocation.tryParse("air"),
            0, 1.0f, 0f, 1, 0, 0xFFFFFF
    );

    public static void clear(){
        MATERIALS.clear();
    }

    public static void register(ResourceLocation id, Material material){
        MATERIALS.put(id,material);
    }

    public static Material getMaterial(ResourceLocation id){
        return MATERIALS.getOrDefault(id, DEFAULT);
    }

    public static Collection<Material> getAllMaterials(){
        return MATERIALS.values();
    }
}

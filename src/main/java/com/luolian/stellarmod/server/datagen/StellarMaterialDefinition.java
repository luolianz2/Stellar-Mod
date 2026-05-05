package com.luolian.stellarmod.server.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用于数据生成的材料定义，包含所有可配置字段。
 */
public class StellarMaterialDefinition {
    private final ResourceLocation itemId;
    private int miningLevel = 0;
    private float miningSpeed = 1.0f;
    private float attackDamage = 0.0f;
    private int durability = 0;
    private int upgradeCost = 1;

    //某个别名相关的材料列表
    private final List<ResourceLocation> aliases = new ArrayList<>();

    private final List<ModifierDef> modifiers = new ArrayList<>();

    public StellarMaterialDefinition(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    public StellarMaterialDefinition miningLevel(int level) {
        this.miningLevel = level;
        return this;
    }

    public StellarMaterialDefinition miningSpeed(float speed) {
        this.miningSpeed = speed;
        return this;
    }

    public StellarMaterialDefinition attackDamage(float damage) {
        this.attackDamage = damage;
        return this;
    }

    public StellarMaterialDefinition durability(int durability) {
        this.durability = durability;
        return this;
    }

    public StellarMaterialDefinition upgradeCost(int cost) {
        this.upgradeCost = cost;
        return this;
    }

    /**
     * 添加别名物品，让多个物品共用同一份材料属性。
     * 别名在数据生成时写入 JSON，运行时加载后统一注册到 MaterialManager。
     */
    //... 表示可变参数：可以传入 0 个、1 个或多个 ResourceLocation 对象
    //Collections.addAll 是一个工具方法，将数组或可变参数中的所有元素一次性添加到目标集合中
    //如果 aliases 为 null 或传入空数组，则没有任何效果（但通常不会传入 null，因为可变参数可以安全为空）
    public StellarMaterialDefinition addAliases(ResourceLocation... aliases) {
        Collections.addAll(this.aliases, aliases);
        return this;
    }

    //添加副词条（仅ID，默认等级1）
    public StellarMaterialDefinition addModifier(String effectId) {
        this.modifiers.add(new ModifierDef(effectId, 1));
        return this;
    }

    //添加副词条（ID + 等级）
    public StellarMaterialDefinition addModifier(String effectId, int level) {
        this.modifiers.add(new ModifierDef(effectId, level));
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("item", itemId.toString());
        json.addProperty("mining_level", miningLevel);
        json.addProperty("mining_speed", miningSpeed);
        json.addProperty("attack_damage", attackDamage);
        json.addProperty("durability", durability);
        json.addProperty("upgrade_cost", upgradeCost);

        if (!aliases.isEmpty()) {
            JsonArray aliasArray = new JsonArray();
            for (ResourceLocation alias : aliases) {
                aliasArray.add(alias.toString());
            }
            json.add("aliases", aliasArray);
        }

        if (!modifiers.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ModifierDef def : modifiers) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", def.effectId);
                //只有等级大于0时才写入（非默认等级可记录，默认为1）
                if (def.level > 1) {
                    obj.addProperty("level", def.level);
                }
                array.add(obj);
            }
            json.add("modifiers", array);
        }
        return json;
    }

    //副词条定义（内部记录），移除了 config，新增 level 字段
    private record ModifierDef(String effectId, int level) {}
}
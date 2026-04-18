package com.luolian.stellarmod.server.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
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

    public StellarMaterialDefinition addModifier(String effectId) {
        this.modifiers.add(new ModifierDef(effectId, null));
        return this;
    }

    public StellarMaterialDefinition addModifier(String effectId, JsonObject config) {
        this.modifiers.add(new ModifierDef(effectId, config));
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

        if (!modifiers.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ModifierDef def : modifiers) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", def.effectId);
                if (def.config != null) {
                    obj.add("config", def.config);
                }
                array.add(obj);
            }
            json.add("modifiers", array);
        }
        return json;
    }

    private record ModifierDef(String effectId, JsonObject config) {}
}
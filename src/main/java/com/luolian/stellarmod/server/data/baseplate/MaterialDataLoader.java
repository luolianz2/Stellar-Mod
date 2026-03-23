package com.luolian.stellarmod.server.data.baseplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MaterialDataLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialDataLoader.class);
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public MaterialDataLoader() {
        super(GSON, "materials");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, ResourceManager manager, ProfilerFiller profiler) {
        MaterialManager.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceMap.entrySet()) {
            ResourceLocation id = entry.getKey();   // 例如 examplemod:iron
            JsonElement json = entry.getValue();
            try {
                Material material = parseMaterial(id, json.getAsJsonObject());
                MaterialManager.register(id, material);
            } catch (Exception e) {
                LOGGER.error("Failed to load material {}: {}", id, e.getMessage());
            }
        }
        LOGGER.info("Loaded {} materials", MaterialManager.getAllMaterials().size());
    }

    private Material parseMaterial(ResourceLocation id, JsonObject json) {
        int miningLevel = json.get("mining_level").getAsInt();
        float miningSpeed = json.get("mining_speed").getAsFloat();
        float attackDamage = json.get("attack_damage").getAsFloat();
        int durability = json.get("durability").getAsInt();
        int enchantability = json.get("enchantability").getAsInt();
        int color = json.get("color").getAsInt();

        return new Material(id, miningLevel, miningSpeed, attackDamage, durability, enchantability, color);
    }
}
package com.luolian.stellarmod.server.data.itemcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.packs.resources.Resource;

import java.util.Collection;
import java.util.Map;
/**
 * 材料数据加载器，负责在资源重载时从数据包中读取并解析材料 JSON 文件。
 * <p>
 * 该类继承自 {@link SimpleJsonResourceReloadListener}，自动扫描所有资源包中
 * {@code assets/<命名空间>/materials/} 目录下的 JSON 文件，并将其解析为
 * {@link Material} 对象注册到 {@link MaterialManager} 中。
 * </p>
 * <p>
 * JSON 文件格式示例：
 * <pre>{@code
 * {
 *   "item": "minecraft:iron_ingot",
 *   "mining_level": 2,
 *   "mining_speed": 6.0,
 *   "attack_damage": 2.0,
 *   "durability": 250,
 *   "enchantAbility": 14,
 *   "color": 16777130
 * }
 * }</pre>
 * 其中 {@code item} 为必填字段，其余字段均有默认值。
 * </p>
 *
 * @see Material
 * @see MaterialManager
 */
public class MaterialDataLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialDataLoader.class);

    /**
     * setPrettyPrinting()：使输出的 JSON 更易读（主要用于日志或导出）<br>
     * disableHtmlEscaping()：防止特殊字符（如 =、<）被转义为 HTML 实体
     */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * 第一个参数传入一个 Gson 实例，用于将 JSON 文本解析为 JsonElement。
     * 第二个参数 "materials" 是子目录名。加载器会扫描所有资源包（包括模组和原版）中 assets/<命名空间>/materials/ 路径下的 JSON 文件
     */
    public MaterialDataLoader() {
        super(GSON, "materials");
    }

    /**
     * 在资源重载时调用，清空旧数据并加载所有材料 JSON 文件。
     *
     * @param resourceMap 键为 JSON 文件的资源位置（如 {@code stellarmod:iron}），
     *                    值为解析后的 JSON 元素
     * @param manager     资源管理器，可用于进一步获取其他资源
     * @param profiler    性能分析器，用于记录加载耗时
     */
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, ResourceManager manager, ProfilerFiller profiler) {
        MaterialManager.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceMap.entrySet()) {
            ResourceLocation fileId = entry.getKey();   // 例如 stellarmod:iron
            JsonElement json = entry.getValue();
            try {
                Material material = parseMaterial(fileId, json.getAsJsonObject());
                MaterialManager.register(material);
            } catch (Exception e) {
                LOGGER.error("Failed to load material from {}: {}", fileId, e.getMessage());
            }
        }
        LOGGER.info("Loaded {} materials", MaterialManager.getAllMaterials().size());
    }

    /**
     * 将单个 JSON 对象解析为 {@link Material} 实例。
     *
     * @param fileId JSON 文件的资源标识，用于错误追踪和调试
     * @param json   代表一个材料定义的 JSON 对象
     * @return 解析后的材料对象
     * @throws IllegalArgumentException 如果必填字段 {@code item} 缺失或无效
     */
    private Material parseMaterial(ResourceLocation fileId, JsonObject json) {
        //必须字段：关联的物品 ID
        ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "item"));
        if (itemId == null) {
            throw new IllegalArgumentException("Invalid or missing 'item' field");
        }

        int miningLevel = GsonHelper.getAsInt(json, "mining_level", 0);
        float miningSpeed = GsonHelper.getAsFloat(json, "mining_speed", 1.0f);
        float attackDamage = GsonHelper.getAsFloat(json, "attack_damage", 0.0f);
        int durability = GsonHelper.getAsInt(json, "durability", 0);
        int enchantAbility = GsonHelper.getAsInt(json, "enchantAbility", 0);
        int color = GsonHelper.getAsInt(json, "color", 0xFFFFFF);

        return new Material(fileId, itemId, miningLevel, miningSpeed, attackDamage, durability, enchantAbility, color);
    }
}
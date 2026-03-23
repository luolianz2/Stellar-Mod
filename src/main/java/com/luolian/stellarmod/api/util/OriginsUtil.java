package com.luolian.stellarmod.api.util;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.compat.OriginLayerSnapshot;
import com.luolian.stellarmod.api.compat.OriginSnapshot;
import com.luolian.stellarmod.api.compat.OriginsTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.github.apace100.origins.origin.Impact;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.common.registry.OriginRegisters;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class OriginsUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(OriginsUtil.class);
    @SuppressWarnings("ConstantConditions")
    public static final ResourceLocation EMPTY_ORIGIN_ID = OriginRegisters.EMPTY.getKey().location(); // origins:empty
    public static final ResourceLocation ORIGINS_ORIGIN_LAYER_ID = location("origin"); // origins:origin
    public static final ResourceLocation DEFAULT_ORIGIN = OriginsUtil.location("human"); // origins:human

    /**
     * 缓存起源ID到起源层ID的映射，以提高通过originId查找所属起源层的效率。
     * Key是originId，Value是其所属的起源层ID。
     */
    private static final Map<ResourceLocation, ResourceLocation> ORIGIN_TO_LAYER_CACHE = new HashMap<>();

    /**
     * @param path 命名空间路径
     * @return 以"origins"为命名空间的命名空间ID对象
     */
    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath("origins", path);
    }

    public static ResourceKey<OriginLayer> getOriginLayerKey(ResourceLocation layerId) {
        if (OriginsAPI.getLayersRegistry().containsKey(layerId)) {
            return ResourceKey.create(OriginsAPI.getLayersRegistry().key(), layerId);
        }
        throw new IllegalArgumentException("Origin Layer with ID " + layerId + " does not exist in the Origins registry.");
    }

    public static ResourceKey<Origin> getOriginKey(ResourceLocation originId) {
        if (OriginsAPI.getOriginsRegistry().containsKey(originId)) {
            return ResourceKey.create(OriginsAPI.getOriginsRegistry().key(), originId);
        }
        throw new IllegalArgumentException("Origin with ID " + originId + " does not exist in the Origins registry.");
    }

    /**
     * 检查给定的originId是否存在于Origins注册表中，特别的“origins:empty”也被视为有效的originId。
     * @param originId 要检查的originId
     * @return originId是否有效
     */
    public static boolean isValidOrigin(ResourceLocation originId) {
        return OriginsAPI.getOriginsRegistry().containsKey(originId);
    }

    /**
     * 检查给定的originId是否是“origins:empty”，该起源意味着玩家在指定起源层没有起源。
     * @param originId 要检查的originId
     * @return originId是否是“origins:empty”
     */
    public static boolean isEmptyOrigin(ResourceLocation originId) {
        return originId.equals(EMPTY_ORIGIN_ID);
    }

    /**
     * 通过originLayerId获取对应起源层的名称。
     * @param id 要获取名称的起源层ID
     * @return 对应起源层的名称文本组件
     */
    @NotNull
    public static Component getOriginLayerName(ResourceLocation id) {
        OriginLayer layer = getOriginLayer(id);
        if (layer != null) {
            return layer.name();
        }
        return Component.translatable("text.stellarmod.unknown_origin_layer");
    }

    /**
     * 通过originId获取对应起源的名称，并根据起源的影响类型设置文本颜色，无影响起源被特殊设置为白色。
     * @param originId 要获取名称的originId
     * @return 对应起源的名称文本组件
     */
    @NotNull
    public static Component getOriginName(ResourceLocation originId) {
        if (isEmptyOrigin(originId)) {
            return Component.literal(Component.translatable("text.stellarmod.empty_origin").getString()).withStyle(ChatFormatting.WHITE);
        }

        Origin origin = getOrigin(originId);
        if (origin == null) return Component.translatable("text.stellarmod.unknown_origin");

        ChatFormatting chatFormatting = origin.getImpact() == Impact.NONE ? ChatFormatting.WHITE : origin.getImpact().getTextStyle();
        return Component.literal(origin.getName().getString()).withStyle(chatFormatting);
    }

    @NotNull
    public static Set<ResourceLocation> getOriginLayerIds() {
        return OriginsAPI.getLayersRegistry().keySet();
    }

    @NotNull
    public static Set<ResourceLocation> getOriginIds() {
        return OriginsAPI.getOriginsRegistry().keySet();
    }

    /**
     * 获取指定起源层中所有起源的ID
     * @param layerId 要获取起源ID的起源层ID
     * @return 指定起源层中所有起源的ID集合
     */
    public static Set<ResourceLocation> getOriginIds(ResourceLocation layerId) {
        OriginLayer originLayer = getOriginLayer(layerId);
        if (originLayer == null) return Set.of();

        return originLayer.conditionedOrigins().stream()
                .flatMap(conditionedOrigin -> conditionedOrigin.origins().stream())
                .flatMap(HolderSet::stream)
                .filter(Holder::isBound)
                .flatMap(holder -> holder.unwrapKey().stream())
                .map(ResourceKey::location)
                .collect(Collectors.toSet());
    }

    /**
     * 使用缓存数据查找起源所属的起源层ID，如果没有找到则返回null。
     * @param originId 要查找的originId
     * @return originId所属的起源层ID，未找到时返回null
     */
    @Nullable
    public static ResourceLocation getLayerIdByOriginId(ResourceLocation originId) {
        return ORIGIN_TO_LAYER_CACHE.getOrDefault(originId, null);
    }

    @NotNull
    public static List<OriginLayerSnapshot> getOriginLayerSnapshots() {
        List<OriginLayerSnapshot> snapshots = new ArrayList<>();
        for (var key : OriginsAPI.getLayersRegistry().keySet()) {
            snapshots.add(OriginLayerSnapshot.from(key));
        }
        return snapshots;
    }

    @NotNull
    public static Map<ResourceLocation, OriginLayerSnapshot> getMappedOriginLayerSnapshots() {
        Map<ResourceLocation, OriginLayerSnapshot> snapshotMap = new HashMap<>();
        for (var key : OriginsAPI.getLayersRegistry().keySet()) {
            snapshotMap.put(key, OriginLayerSnapshot.from(key));
        }
        return snapshotMap;
    }

    @NotNull
    public static List<OriginSnapshot> getOriginSnapshots() {
        List<OriginSnapshot> snapshots = new ArrayList<>();
        for (var key : OriginsAPI.getOriginsRegistry().keySet()) {
            snapshots.add(OriginSnapshot.from(key));
        }
        return snapshots;
    }

    @NotNull
    public static Map<ResourceLocation, OriginSnapshot> getMappedOriginSnapshots() {
        Map<ResourceLocation, OriginSnapshot> snapshotMap = new HashMap<>();
        for (var key : OriginsAPI.getOriginsRegistry().keySet()) {
            snapshotMap.put(key, OriginSnapshot.from(key));
        }
        return snapshotMap;
    }

    /**
     * 通过originId从Origins注册表中获取Origin对象。
     * @param originId 要获取的Origin的ID
     * @return 对应originId的Origin对象
     */
    public static Origin getOrigin(ResourceLocation originId) {
        return OriginsAPI.getOriginsRegistry().get(originId);
    }

    public static OriginLayer getOriginLayer(ResourceLocation layerId) {
        return OriginsAPI.getLayersRegistry().get(layerId);
    }

    /**
     * 获取通用的FTB图标。
     * @return 通用的起源图标
     */
    public static Icon getOriginIcon() {
        return Icon.getIcon(StellarMod.textureLocation("icons/origin.png"));
    }

    /**
     * 通过originId获取对应起源的FTB图标。
     * @param originId 要获取图标的Origin的ID
     * @return 对应起源的FTB图标
     */
    public static Icon getOriginIcon(ResourceLocation originId) {
        if (isEmptyOrigin(originId) || !isValidOrigin(originId)) {
            return getOriginIcon();
        }

        return ItemIcon.getItemIcon(OriginsUtil.getOrigin(originId).getIcon());
    }

    /**
     * 检查玩家是否拥有指定的起源。
     * @param container 玩家的起源能力容器
     * @param layerId 要检查的起源所属的起源层ID
     * @param originId 要检查的起源ID
     * @return 玩家是否拥有指定的起源
     */
    public static boolean hasOrigin(IOriginContainer container, ResourceLocation layerId, ResourceLocation originId) {
        if (layerId == null) return hasOrigin(container, originId);

        return container.getOrigin(getOriginLayerKey(layerId)).equals(getOriginKey(originId));
    }

    /**
     * 检查玩家在任意起源层上是否拥有指定的起源，需要遍历所有起源层进行检查，性能较低。
     * @param container 玩家的起源能力容器
     * @param originId 要检查的起源ID
     * @return 玩家是否拥有指定的起源
     * @see #hasOrigin(IOriginContainer, ResourceLocation, ResourceLocation)
     */
    @Deprecated
    public static boolean hasOrigin(IOriginContainer container, ResourceLocation originId) {
        for (var layerId : OriginsAPI.getLayersRegistry().keySet()) {
            if (hasOrigin(container, layerId, originId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建起源ID到起源层ID的缓存映射，频繁调用此方法会影响性能。
     */
    public static void buildOriginToLayerCache() {
        ORIGIN_TO_LAYER_CACHE.clear();
        for (ResourceLocation layerId : OriginsAPI.getLayersRegistry().keySet()) {
            for (ResourceLocation originId : getOriginIds(layerId)) {
                ORIGIN_TO_LAYER_CACHE.put(originId, layerId);
            }
        }
        LOGGER.info("Built Origin to Layer cache with {} entries.", ORIGIN_TO_LAYER_CACHE.size());
    }

    /**
     * 处理玩家起源变化，用于检查FTB任务。
     * @param serverPlayer 起源发生变化的玩家对象
     */
    public static void handleOriginChange(ServerPlayer serverPlayer) {
        TeamData teamData = TeamData.get(serverPlayer);
        if (teamData == null || teamData.isLocked()) return;

        if (teamData.getFile() instanceof ServerQuestFile sqf) {
            sqf.withPlayerContext(serverPlayer, () -> {
                for (Task task : teamData.getFile().getSubmitTasks()) {
                    if (task instanceof OriginsTask originsTask && teamData.canStartTasks(originsTask.getQuest())) {
                        originsTask.submitTask(teamData, serverPlayer);
                    }
                }
            });
        }
    }
}

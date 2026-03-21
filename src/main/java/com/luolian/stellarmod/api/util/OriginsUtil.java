package com.luolian.stellarmod.api.util;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.compat.OriginsTask;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.common.capabilities.OriginContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class OriginsUtil {
    /**
     * @param path 命名空间路径
     * @return 以"origins"为命名空间的命名空间ID对象
     */
    public static ResourceLocation originsLocation(String path) {
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

    public static boolean isValidOrigin(ResourceLocation originId) {
        return OriginsAPI.getOriginsRegistry().containsKey(originId);
    }

    public static Component getOriginName(ResourceLocation originId) {
        Origin origin = getOrigin(originId);
        if (origin != null) {
            return origin.getName();
        }
        return Component.literal("Unknown Origin");
    }

    public static Origin getOrigin(ResourceLocation originId) {
        return OriginsAPI.getOriginsRegistry().get(originId);
    }

    public static Icon getOriginIcon() {
        return Icon.getIcon(StellarMod.textureLocation("icons/origin.png"));
    }

    public static Icon getOriginIcon(ResourceLocation originId) {
        return ItemIcon.getItemIcon(OriginsUtil.getOrigin(originId).getIcon());
    }

    public static boolean hasOrigin(OriginContainer container, ResourceLocation originId) {
        return OriginsAPI.getOriginsRegistry().containsKey(originId);
    }

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

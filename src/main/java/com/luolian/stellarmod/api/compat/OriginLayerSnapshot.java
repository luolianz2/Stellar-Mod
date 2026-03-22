package com.luolian.stellarmod.api.compat;

import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.origin.GuiTitle;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record OriginLayerSnapshot (
        ResourceLocation id,
        Component name,
        GuiTitle guiTitle
) {
    public static OriginLayerSnapshot from(ResourceLocation layerId, OriginLayer layer) {
        return new OriginLayerSnapshot(layerId, layer.name(), layer.title());
    }

    public static OriginLayerSnapshot from(ResourceLocation layerId) {
        OriginLayer layer = OriginsAPI.getLayersRegistry().get(layerId);
        if (layer != null) {
            return from(layerId, layer);
        }
        throw new IllegalArgumentException("Origin Layer with ID " + layerId + " does not exist.");
    }
}

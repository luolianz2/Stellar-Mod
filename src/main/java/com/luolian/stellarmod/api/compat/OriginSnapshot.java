package com.luolian.stellarmod.api.compat;

import io.github.apace100.origins.origin.Impact;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record OriginSnapshot(
        ResourceLocation id,
        ItemStack icon,
        Component name,
        Component description,
        Impact impact
) {
    private static OriginSnapshot from(ResourceLocation id, Origin origin) {
        return new OriginSnapshot(id, origin.getIcon(), origin.getName(), origin.getDescription(), origin.getImpact());
    }

    public static OriginSnapshot from(ResourceLocation originId) {
        Origin origin = OriginsAPI.getOriginsRegistry().get(originId);
        if (origin != null) {
            return from(originId, origin);
        }
        throw new IllegalArgumentException("Origin with ID " + originId + " does not exist.");
    }
}

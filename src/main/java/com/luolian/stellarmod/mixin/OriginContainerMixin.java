package com.luolian.stellarmod.mixin;

import com.luolian.stellarmod.api.util.OriginsUtil;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.common.capabilities.OriginContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OriginContainer.class, remap = false)
public abstract class OriginContainerMixin {
    @Shadow public abstract @NotNull Player getOwner();

    @Inject(
            method = "setOriginInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/apace100/origins/util/ChoseOriginCriterion;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onSetOrigin(@NotNull ResourceKey<OriginLayer> layer, @NotNull ResourceKey<Origin> origin, boolean handlePowers, CallbackInfo ci) {
        if (this.getOwner() instanceof ServerPlayer serverPlayer) {
            OriginsUtil.handleOriginChange(serverPlayer);
        }
    }
}

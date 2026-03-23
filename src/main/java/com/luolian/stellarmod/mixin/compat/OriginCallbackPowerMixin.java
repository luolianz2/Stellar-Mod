package com.luolian.stellarmod.mixin.compat;

import com.luolian.stellarmod.api.util.OriginsUtil;
import io.github.apace100.origins.power.OriginsCallbackPower;
import io.github.edwinmindcraft.origins.common.power.configuration.OriginsCallbackConfiguration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OriginsCallbackPower.class, remap = false)
public class OriginCallbackPowerMixin {
    @Inject(
            method = "onChosen(Lio/github/edwinmindcraft/origins/common/power/configuration/OriginsCallbackConfiguration;Lnet/minecraft/world/entity/Entity;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/edwinmindcraft/apoli/api/power/configuration/ConfiguredEntityAction;execute(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onOriginChosen(OriginsCallbackConfiguration configuration, Entity entity, boolean isOrb, CallbackInfo ci) {
        if (entity instanceof ServerPlayer serverPlayer) {
            OriginsUtil.handleOriginChange(serverPlayer);
        }
    }
}

package com.luolian.stellarmod.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import com.luolian.stellarmod.api.internal.QuestVisibilityController;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Quest.class, remap = false)
public class QuestMixin implements QuestVisibilityController {
    @Unique
    private boolean stellar$alwaysInvisible = false;

    @Override
    public void stellar$setAlwaysInvisible(boolean alwaysInvisible) {
        this.stellar$alwaysInvisible = alwaysInvisible;
    }

    @Override
    public boolean stellar$isAlwaysInvisible() {
        return this.stellar$alwaysInvisible;
    }

    @Inject(
            method = "isVisible",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onIsVisible(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        if (this.stellar$alwaysInvisible) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "fillConfigGroup",
            at = @At("TAIL")
    )
    private void onFillConfigGroup(ConfigGroup config, CallbackInfo ci, @Local(name = "visibility") ConfigGroup visibility) {
        visibility.addBool(
                "always_invisible",
                this.stellar$alwaysInvisible,
                v -> this.stellar$alwaysInvisible = v,
                false
        );
    }

    @Inject(
            method = "writeData",
            at = @At("TAIL")
    )
    private void onWriteData(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("stellar$alwaysInvisible", this.stellar$alwaysInvisible);
    }

    @Inject(
            method = "readData",
            at = @At("TAIL")
    )
    private void onReadData(CompoundTag nbt, CallbackInfo ci) {
        this.stellar$alwaysInvisible = nbt.getBoolean("stellar$alwaysInvisible");
    }

    @Inject(
            method = "writeNetData",
            at = @At("TAIL")
    )
    private void onWriteNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(this.stellar$alwaysInvisible);
    }

    @Inject(
            method = "readNetData",
            at = @At("TAIL")
    )
    private void onReadNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        this.stellar$alwaysInvisible = buffer.readBoolean();
    }
}

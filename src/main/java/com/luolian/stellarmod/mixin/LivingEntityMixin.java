package com.luolian.stellarmod.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.luolian.stellarmod.server.effect.StellarMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract boolean hasEffect(MobEffect pEffect);

    @Shadow @Nullable
    public abstract MobEffectInstance getEffect(MobEffect pEffect);

    @ModifyArg(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            ),
            index = 1
    )
    private Object modifyPutEffect(Object value, @Share("modifiedInstance") LocalRef<MobEffectInstance> modifiedInstance) {
        if (value instanceof MobEffectInstance instance) {
            MobEffectInstance modified = stellarmod$modifyMobEffectInstance(instance);
            modifiedInstance.set(modified);
            return modified;
        }
        return value;
    }

    @ModifyArg(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;onEffectAdded(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)V"
            ),
            index = 0
    )
    private MobEffectInstance modifyOnEffectAddedEffect(MobEffectInstance original, @Share("modifiedInstance") LocalRef<MobEffectInstance> modifiedInstance) {
        MobEffectInstance modified = modifiedInstance.get();
        return modified != null ? modified : original;
    }

    @Unique
    private MobEffectInstance stellarmod$modifyMobEffectInstance(MobEffectInstance instance) {
        if (this.hasEffect(StellarMobEffects.GRAIN_ALCOHOL.get())
                && instance.getEffect() != StellarMobEffects.GRAIN_ALCOHOL.get()
        ) {
            MobEffectInstance grainAlcoholInstance = this.getEffect(StellarMobEffects.GRAIN_ALCOHOL.get());
            if (grainAlcoholInstance == null) return instance;

            int newAmplifier = instance.getAmplifier() + Math.round(grainAlcoholInstance.getAmplifier() / 2.0F);
            int newDuration = instance.getDuration() + (grainAlcoholInstance.getAmplifier() + 1) * 100; // 每级增加5s的持续时间

            return new MobEffectInstance(
                    instance.getEffect(),
                    newDuration,
                    newAmplifier,
                    instance.isAmbient(),
                    instance.isVisible()
            );
        }

        return instance;
    }
}

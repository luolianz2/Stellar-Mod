package com.luolian.stellarmod.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.effect.StellarMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID)
public class MobEffectEventListener {
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == MobEffects.REGENERATION) return;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.hasEffect(StellarMobEffects.GRAIN_ALCOHOL.get())) {
            MobEffectInstance instance = livingEntity.getEffect(StellarMobEffects.GRAIN_ALCOHOL.get());
            if (instance == null) return;

            MobEffectInstance regenerationEffect = new MobEffectInstance(
                    MobEffects.REGENERATION,
                    100,
                    Math.min(instance.getAmplifier() + 2, 255), // 防止byte溢出
                    true,
                    true,
                    true
            );

            livingEntity.addEffect(regenerationEffect);
        }
    }
}

package com.luolian.stellarmod.server.potion;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.effect.StellarMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StellarPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, StellarMod.MOD_ID);

    // 基础药水（3分钟）
    public static final RegistryObject<Potion> GRAIN_ALCOHOL_POTION =
            POTIONS.register("grain_alcohol",
                    () -> new Potion(new MobEffectInstance(StellarMobEffects.GRAIN_ALCOHOL.get(),
                            3600, 0))
            );

    // 延长版药水（8分钟）
    public static final RegistryObject<Potion> LONG_GRAIN_ALCOHOL_POTION =
            POTIONS.register("long_grain_alcohol",
                    () -> new Potion(new MobEffectInstance(StellarMobEffects.GRAIN_ALCOHOL.get(),
                            9600, 0))
            );

    // 强化版药水（1分30秒，等级2）
    public static final RegistryObject<Potion> STRONG_GRAIN_ALCOHOL_POTION =
            POTIONS.register("strong_grain_alcohol",
                    () -> new Potion(new MobEffectInstance(StellarMobEffects.GRAIN_ALCOHOL.get(),
                            1800, 1))
            );

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}

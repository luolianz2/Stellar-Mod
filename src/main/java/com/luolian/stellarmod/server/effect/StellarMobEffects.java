package com.luolian.stellarmod.server.effect;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.effect.effects.GrainAlcoholMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StellarMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, StellarMod.MOD_ID);

    public static final RegistryObject<MobEffect> GRAIN_ALCOHOL =
            MOB_EFFECTS.register("grain_alcohol", () -> new GrainAlcoholMobEffect());

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}

package com.luolian.stellarmod.worldgen.biome;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class StellarBiomes {
    public static final ResourceKey<Biome> STELLAR_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(StellarMod.MOD_ID, "stellar_biome"));

    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(STELLAR_BIOME, createStellarBiome(context));
    }

    //太空环境
    private static Biome createStellarBiome(BootstapContext<Biome> context) {  // 这里也改回
        //获取必要的HolderGetter用于构建BiomeGenerationSettings（为空，无地形等）
        HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carverGetter = context.lookup(Registries.CONFIGURED_CARVER);

        //生物生成为空
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        //传入（空，无地形等）
        BiomeGenerationSettings.Builder generationBuilder =
                new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);

        //特效模拟太空
        BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder()
                .skyColor(0x000000)
                .fogColor(0x000000)
                .waterColor(0x3f76e4)
                .waterFogColor(0x050533);

        //构建并返回生态群系
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.8f)
                .downfall(0.0f)
                .specialEffects(effectsBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(generationBuilder.build())
                .build();
    }
}
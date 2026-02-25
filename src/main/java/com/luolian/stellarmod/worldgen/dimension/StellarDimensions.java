package com.luolian.stellarmod.worldgen.dimension;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.worldgen.biome.StellarBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;


public class StellarDimensions {
    public static final ResourceKey<LevelStem> STELLAR_SPACE_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(StellarMod.MOD_ID, "stellar_space"));
    public static final ResourceKey<Level> STELLAR_SPACE_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(StellarMod.MOD_ID,"stellar_space"));
    public static final ResourceKey<DimensionType> STELLAR_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(StellarMod.MOD_ID, "stellar_type"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(STELLAR_DIM_TYPE, new DimensionType(
                OptionalLong.empty(),                  // 1. 修改：关闭昼夜周期（太空无昼夜）
                false,
                false,
                false,
                false,
                1.0,
                false,                                 // 7. 修改：关闭降水（太空无大气）
                false,
                0,
                512,                                   // 10. 优化：提升维度高度至512，增加太空空旷感
                512,                                   // 11. 同步修改渲染高度
                BlockTags.INFINIBURN_END,              // 12. 修改：替换为末地无限燃烧标签（更适配太空）
                BuiltinDimensionTypes.END_EFFECTS,     // 13. 修改：替换为末地视觉特效（黑色天空/雾，贴合太空）
                1.0f,
                new DimensionType.MonsterSettings(false,false,  ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(StellarBiomes.STELLAR_BIOME)),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED));

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(StellarDimensions.STELLAR_DIM_TYPE), wrappedChunkGenerator);
        context.register(STELLAR_SPACE_KEY, stem);
    }
}

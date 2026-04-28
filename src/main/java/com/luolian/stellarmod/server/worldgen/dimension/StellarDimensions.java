package com.luolian.stellarmod.server.worldgen.dimension;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.worldgen.biome.StellarBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;

import java.util.OptionalLong;


public class StellarDimensions {
    public static final ResourceKey<LevelStem> STELLAR_SPACE_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            StellarMod.location("stellar_space"));
    public static final ResourceKey<Level> STELLAR_SPACE_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            StellarMod.location("stellar_space"));
    public static final ResourceKey<DimensionType> STELLAR_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            StellarMod.location("stellar_type"));

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
        EmptyChunkGenerator emptyChunkGenerator = new EmptyChunkGenerator(
                biomeRegistry.getOrThrow(StellarBiomes.STELLAR_BIOME)
        );

        LevelStem stem = new LevelStem(
                dimTypes.getOrThrow(StellarDimensions.STELLAR_DIM_TYPE),
                emptyChunkGenerator
        );
        context.register(STELLAR_SPACE_KEY, stem);
    }
}

package com.luolian.stellarmod.worldgen.dimension;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;

import java.util.OptionalLong;


public class StellarDimensions {
    public static final ResourceKey<LevelStem> STELLAR_SPACE_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(StellarMod.MOD_ID, "stellar_space"));
    public static final ResourceKey<Level> STELLAR_SPACE_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(StellarMod.MOD_ID,"stellar_space"));
    public static final ResourceKey<DimensionType> STELLAR_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(StellarMod.MOD_ID, "stellar_type"));

}

package com.luolian.stellarmod.server.datagen.materials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.datagen.StellarMaterialDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StellarMaterialProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput packOutput;
    private final List<StellarMaterialDefinition> materials = new ArrayList<>();

    public StellarMaterialProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        registerMaterials();
        return CompletableFuture.allOf(materials.stream()
                .map(def -> saveMaterial(cachedOutput, def))
                .toArray(CompletableFuture[]::new));
    }

    private void registerMaterials() {
        //木(副词条参数已硬编码在效果类中，不再通过config传递)
        //别名涵盖所有原版木板，共用同一份材料属性
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.OAK_PLANKS))
                .miningLevel(1)
                .miningSpeed(2.0f)
                .attackDamage(0.0f)
                .durability(100)
                .upgradeCost(3)
                .addAliases(
                        BuiltInRegistries.ITEM.getKey(Items.SPRUCE_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.BIRCH_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.JUNGLE_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.ACACIA_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.DARK_OAK_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.MANGROVE_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.CHERRY_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.BAMBOO_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.CRIMSON_PLANKS),
                        BuiltInRegistries.ITEM.getKey(Items.WARPED_PLANKS)
                )
                .addModifier("stellarmod:electromagnetic")
        );

        //石
        //别名涵盖圆石变种，共用同一份材料属性
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.COBBLESTONE))
                .miningLevel(1)
                .miningSpeed(4.0f)
                .attackDamage(2.0f)
                .durability(150)
                .upgradeCost(3)
                .addAliases(
                        BuiltInRegistries.ITEM.getKey(Items.COBBLED_DEEPSLATE),
                        BuiltInRegistries.ITEM.getKey(Items.BLACKSTONE)
                )
                .addModifier("stellarmod:electromagnetic")
        );

        //金锭
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.GOLD_INGOT))
                .miningLevel(1)
                .miningSpeed(12.0f)
                .attackDamage(0.0f)
                .durability(50)
                .upgradeCost(3)
                .addModifier("stellarmod:electromagnetic")
        );

        //铜锭
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.COPPER_INGOT))
                .miningLevel(1)
                .miningSpeed(5.0f)
                .attackDamage(2.0f)
                .durability(200)
                .upgradeCost(3)
                .addModifier("stellarmod:electromagnetic")
        );

        //铁锭 (副词条参数已硬编码在效果类中，不再通过config传递)
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT))
                .miningLevel(2)
                .miningSpeed(6.0f)
                .attackDamage(2.0f)
                .durability(300)
                .upgradeCost(3)
                .addModifier("stellarmod:electromagnetic")
        );

        //钻石
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.DIAMOND))
                .miningLevel(3)
                .miningSpeed(8.0f)
                .attackDamage(3.0f)
                .durability(1600)
                .upgradeCost(3)
                .addModifier("stellarmod:durable",1)
                .addModifier("stellarmod:precision_collection",1)
        );

        //下界合金锭
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.NETHERITE_INGOT))
                .miningLevel(4)
                .miningSpeed(9.0f)
                .attackDamage(4.0f)
                .durability(2100)
                .upgradeCost(3)
                .addModifier("stellarmod:durable",1)
        );
    }

    private CompletableFuture<?> saveMaterial(CachedOutput cachedOutput, StellarMaterialDefinition def) {
        String itemPath = def.toJson().get("item").getAsString();
        ResourceLocation itemId = ResourceLocation.tryParse(itemPath);
        if (itemId == null) return CompletableFuture.completedFuture(null);

        String fileName = itemId.getPath().replace('/', '_') + ".json";
        Path path = packOutput.getOutputFolder()
                .resolve("data/" + StellarMod.MOD_ID + "/materials/" + fileName);

        return DataProvider.saveStable(cachedOutput, def.toJson(), path);
    }

    @Override
    public String getName() {
        return "StellarMod Materials";
    }
}
package com.luolian.stellarmod.server.datagen.materials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
        //铁锭
        JsonObject electromagneticConfig = new JsonObject();
        electromagneticConfig.addProperty("radius", 5.0);
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT))
                .miningLevel(2)
                .miningSpeed(6.0f)
                .attackDamage(2.0f)
                .durability(250)
                .upgradeCost(3)
                .addModifier("stellarmod:electromagnetic", electromagneticConfig)
        );

        //下界合金锭
        materials.add(new StellarMaterialDefinition(BuiltInRegistries.ITEM.getKey(Items.NETHERITE_INGOT))
                .miningLevel(4)
                .miningSpeed(6.0f)
                .attackDamage(5.0f)
                .durability(10500)
                .upgradeCost(3)
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
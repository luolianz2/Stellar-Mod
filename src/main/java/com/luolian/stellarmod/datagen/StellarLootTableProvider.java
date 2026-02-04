//package com.luolian.stellarmod.datagen;
//
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.loot.LootTableProvider;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
//
//import java.util.List;
//import java.util.Set;
//
//public class StellarLootTableProvider {
//    public static LootTableProvider create(PackOutput packOutput) {
//        return new LootTableProvider(packOutput, Set.of(), List.of(
//                new LootTableProvider.SubProviderEntry(StellarBlockLootTablesProvider::new, LootContextParamSet.BLOCK)
//        ))
//    }
//}

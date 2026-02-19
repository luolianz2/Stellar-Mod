package com.luolian.stellarmod.datagen;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

//数据生成主类，不要改
@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new StellarRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), StellarLootTableProvider.create(packOutput));

        generator.addProvider(event.includeClient(), new StellarBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new StellarItemModelProvider(packOutput, existingFileHelper));

        StellarBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
                new StellarBlockTagGenerator(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new StellarItemTagGenerator(packOutput, lookupProvider,
                blockTagGenerator.contentsGetter(), existingFileHelper));
    }
}

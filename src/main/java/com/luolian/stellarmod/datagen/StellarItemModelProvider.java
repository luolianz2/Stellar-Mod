package com.luolian.stellarmod.datagen;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.item.StellarItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

//用于创建物品模型json文件
public class StellarItemModelProvider extends ItemModelProvider {
    public StellarItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, StellarMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {   //添加物品模型
        simpleItem(StellarItems.BLUSH_FACE);
        simpleItem(StellarItems.METAL_DETECTOR);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(StellarMod.MOD_ID, "item/" + item.getId().getPath()));
    }
}

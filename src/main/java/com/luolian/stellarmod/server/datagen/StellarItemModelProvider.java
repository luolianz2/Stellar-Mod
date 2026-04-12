package com.luolian.stellarmod.server.datagen;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.server.item.StellarItems;
import net.minecraft.data.PackOutput;
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
        simpleItem(StellarItems.SAPPHIRE_CRYSTAL);
        simpleItem(StellarItems.TOOL_CORE);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                "item/generated").texture("layer0",
                StellarMod.location("item/" + item.getId().getPath()));
    }
}

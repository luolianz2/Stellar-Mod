package com.luolian.stellarmod.datagen;


import com.luolian.stellarmod.block.StellarBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class StellarBlockLootTablesProvider extends BlockLootSubProvider {
    protected StellarBlockLootTablesProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(StellarBlocks.Rainbow_Block.get());
        dropSelf(StellarBlocks.Dimension_Block.get());
        dropSelf(StellarBlocks.Space_Station_Block.get());
        dropSelf(StellarBlocks.Coil_Block.get());
    }
//    protected LootTable.Builder createCopperOreLikeDrops(Block p_251306_, Item p_251305_) {
//        return createSilkTouchDispatchTable(p_251306_, this.applyExplosionDecay(p_251306_, LootItem.lootTableItem(p_251305_)
//                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
//                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
//    }

    @Override
    protected Iterable<Block>  getKnownBlocks() {
        return StellarBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}

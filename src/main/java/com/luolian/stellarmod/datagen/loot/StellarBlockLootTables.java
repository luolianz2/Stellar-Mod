package com.luolian.stellarmod.datagen.loot;


import com.luolian.stellarmod.server.block.StellarBlocks;
import com.luolian.stellarmod.server.item.StellarItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

//用于创建物品掉落json文件
public class StellarBlockLootTables extends BlockLootSubProvider{
    public StellarBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() { //添加挖掘后掉落自身的物品（不需要掉落的需要在注册的时候写.noOcclusion())
        //掉落自身的方块
        this.dropSelf(StellarBlocks.RAINBOW_BLOCK.get());
        this.dropSelf(StellarBlocks.COIL_BLOCK.get());
        this.dropSelf(StellarBlocks.DIMENSION_BLOCK.get());
        this.dropSelf(StellarBlocks.SPACE_STATION_BLOCK.get());
        this.dropSelf(StellarBlocks.SPACE_STATION_GLASS_BLOCK.get());
        this.dropSelf(StellarBlocks.SAPPHIRE_CRYSTAL_BLOCK.get());

        //添加类矿物掉落
        this.add(StellarBlocks.SAPPHIRE_CRYSTAL_ORE.get(),
                block -> createCopperLikeOreDrops(StellarBlocks.SAPPHIRE_CRYSTAL_ORE.get(), StellarItems.SAPPHIRE_CRYSTAL.get()));
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block p_251306_, Item item) {  //矿物掉落，遵循的原版铜矿石掉落
        return createSilkTouchDispatchTable(p_251306_,
                this.applyExplosionDecay(p_251306_,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return StellarBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}

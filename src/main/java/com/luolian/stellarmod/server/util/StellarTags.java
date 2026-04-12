package com.luolian.stellarmod.server.util;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class StellarTags {
    public static class Blocks {
        public static final TagKey<Block> METAL_DETECTOR_VALUABLES = tag("metal_detector_valuables");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(StellarMod.location(name));
        }
    }

    public  static class Items {
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(StellarMod.location(name));
        }
    }

}

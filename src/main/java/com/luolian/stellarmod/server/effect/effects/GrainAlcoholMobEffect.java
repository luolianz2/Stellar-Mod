package com.luolian.stellarmod.server.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 状态效果功能详见{@linkplain com.luolian.stellarmod.mixin.LivingEntityMixin}
 */
public class GrainAlcoholMobEffect extends MobEffect {
    public GrainAlcoholMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFDA4AF);
    }

    public static Ingredient getInputIngredient() {
        return Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.NIGHT_VISION));
    }

    public static Ingredient getIngredient() {
        return Ingredient.of(Items.GOLDEN_APPLE);
    }
}

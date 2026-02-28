package com.luolian.stellarmod.effect.effects;

import com.luolian.stellarmod.potion.StellarPotions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.entity.living.MobEffectEvent;

/**
 * 状态效果功能详见{@linkplain MobEffectEventListener#onMobEffectAdded(MobEffectEvent.Added)}
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

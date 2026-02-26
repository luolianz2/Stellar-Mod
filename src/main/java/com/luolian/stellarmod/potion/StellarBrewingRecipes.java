package com.luolian.stellarmod.potion;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.effect.effects.GrainAlcoholMobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = StellarMod.MOD_ID)
public class StellarBrewingRecipes {
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        registerRecipes(
                GrainAlcoholMobEffect.getInputIngredient(),
                GrainAlcoholMobEffect.getIngredient(),
                StellarPotions.GRAIN_ALCOHOL_POTION.get(),
                StellarPotions.LONG_GRAIN_ALCOHOL_POTION.get(),
                StellarPotions.STRONG_GRAIN_ALCOHOL_POTION.get()
        );
    }

    /**
     * 注册药水的酿造配方，自动注册加长药水和强化药水
     * @param input 酿造该药水所需的基础药水（通常是粗制药水）
     * @param ingredient 酿造该药水所需的材料（如金胡萝卜）
     * @param potion 该酿造配方的结果
     * @param longPotion 该药水的加长版本（使用红石）
     * @param strongPotion 该药水的强化版本（使用萤石）
     * @see StellarBrewingRecipes#registerRecipes(Ingredient, Ingredient, Potion)
     */
    private static void registerRecipes(Ingredient input, Ingredient ingredient, Potion potion, Potion longPotion, Potion strongPotion) {
        registerRecipes(input, ingredient, potion);
        BrewingRecipeRegistry.addRecipe(createIngredient(potion), Ingredient.of(Items.REDSTONE), createPotion(longPotion));
        BrewingRecipeRegistry.addRecipe(createIngredient(potion), Ingredient.of(Items.GLOWSTONE_DUST), createPotion(strongPotion));
    }

    /**
     * 注册药水的酿造配方，喷溅型、滞留型和药水箭minecraft会自动生成
     * @param input 酿造该药水所需的基础药水（通常是粗制药水）
     * @param ingredient 酿造该药水所需的材料（如金胡萝卜）
     * @param potion 该酿造配方的结果
     * @see BrewingRecipeRegistry#addRecipe(Ingredient, Ingredient, ItemStack)
     */
    private static void registerRecipes(Ingredient input, Ingredient ingredient, Potion potion) {
        BrewingRecipeRegistry.addRecipe(input, ingredient, createPotion(potion));
    }

    private static ItemStack createPotion(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    private static Ingredient createIngredient(Potion potion) {
        return Ingredient.of(createPotion(potion));
    }
}

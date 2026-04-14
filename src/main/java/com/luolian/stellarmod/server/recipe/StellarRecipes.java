package com.luolian.stellarmod.server.recipe;

import com.luolian.stellarmod.StellarMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StellarRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, StellarMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CraftingAreaRecipe>> CRAFTING_AREA_SERIALIZER =
            SERIALIZERS.register("crafting_area", () -> CraftingAreaRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}

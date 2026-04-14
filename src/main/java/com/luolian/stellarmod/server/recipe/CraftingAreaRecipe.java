package com.luolian.stellarmod.server.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static com.luolian.stellarmod.StellarMod.location;

public class CraftingAreaRecipe implements Recipe<SimpleContainer> {

    //7个输入槽的原料要求
    private final NonNullList<Ingredient> inputItems;
    //每个输入槽消耗的数量（长度固定为7）
    private final int[] counts;
    //配方输出（在预览/合成时作为产物类型参考）
    private final ItemStack output;
    //配方唯一标识
    private final ResourceLocation id;

    public CraftingAreaRecipe(NonNullList<Ingredient> inputItems, int[] counts, ItemStack output, ResourceLocation id) {
        this.inputItems = inputItems;
        this.counts = counts;
        this.output = output;
        this.id = id;
    }

    //获取指定槽位应消耗的物品数量
    public int getConsumeCount(int slot) {
        return counts[slot];
    }

    //匹配逻辑：检查容器中前7个槽位是否满足原料要求，且数量足够消耗
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) return false;
        for (int i = 0; i < 7; i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!inputItems.get(i).test(stack) || stack.getCount() < counts[i]) {
                return false;
            }
        }
        return true;
    }

    //根据容器内容合成输出物品（这里仅返回 output 副本，实际属性叠加由方块实体处理）
    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    //获取配方结果物品（用于显示）
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    //自定义配方类型定义
    public static class Type implements RecipeType<CraftingAreaRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_area";
    }

    //序列化器：负责 JSON 解析与网络传输
    public static class Serializer implements RecipeSerializer<CraftingAreaRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = location("crafting_area");

        @Override
        public CraftingAreaRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            //解析输出物品
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            //解析输入原料数组（长度为7）
            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(7, Ingredient.EMPTY);
            int[] counts = new int[7];

            for (int i = 0; i < 7; i++) {
                JsonElement element = ingredients.get(i);
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    inputs.set(i, Ingredient.fromJson(obj.get("item")));
                    counts[i] = GsonHelper.getAsInt(obj, "count", 1);
                } else {
                    //兼容旧格式：直接写物品ID字符串，数量默认为1
                    inputs.set(i, Ingredient.fromJson(element));
                    counts[i] = 1;
                }
            }
            return new CraftingAreaRecipe(inputs, counts, output, pRecipeId);
        }

        @Nullable
        @Override
        public CraftingAreaRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            int size = pBuffer.readInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            int[] counts = new int[size];
            for (int i = 0; i < size; i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
                counts[i] = pBuffer.readVarInt();
            }
            ItemStack output = pBuffer.readItem();
            return new CraftingAreaRecipe(inputs, counts, output, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, CraftingAreaRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());
            for (int i = 0; i < pRecipe.inputItems.size(); i++) {
                pRecipe.inputItems.get(i).toNetwork(pBuffer);
                pBuffer.writeVarInt(pRecipe.counts[i]);
            }
            pBuffer.writeItemStack(pRecipe.output, false);
        }
    }
}
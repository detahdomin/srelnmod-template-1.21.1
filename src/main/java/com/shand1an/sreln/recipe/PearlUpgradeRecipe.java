package com.shand1an.sreln.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shand1an.sreln.item.PearlLevelUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class PearlUpgradeRecipe implements SmithingRecipe {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public PearlUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return this.template.test(input.getItem(0))
                && this.base.test(input.getItem(1))
                && this.addition.test(input.getItem(2));
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack inputBase = input.getItem(1);
        int currentLevel = PearlLevelUtil.getPearlLevel(inputBase);
        ItemStack output = inputBase.copyWithCount(1);
        PearlLevelUtil.setPearlLevel(output, currentLevel + 1);
        return output;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PEARL_UPGRADE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public static class Serializer implements RecipeSerializer<PearlUpgradeRecipe> {

        public static final MapCodec<PearlUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(r -> r.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition),
                        ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result)
                ).apply(instance, PearlUpgradeRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, PearlUpgradeRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
                        Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
                        Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
                        ItemStack.STREAM_CODEC, r -> r.result,
                        PearlUpgradeRecipe::new
                );

        @Override
        public MapCodec<PearlUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PearlUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
package com.shand1an.sreln.recipe;

import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, srelnMod.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, srelnMod.MODID);

    public static final Supplier<RecipeType<PearlUpgradeRecipe>> PEARL_UPGRADE_TYPE =
            RECIPE_TYPES.register("pearl_upgrade", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "pearl_upgrade")));

    public static final Supplier<RecipeSerializer<PearlUpgradeRecipe>> PEARL_UPGRADE_SERIALIZER =
            RECIPE_SERIALIZERS.register("pearl_upgrade", PearlUpgradeRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
        RECIPE_TYPES.register(bus);
    }
}
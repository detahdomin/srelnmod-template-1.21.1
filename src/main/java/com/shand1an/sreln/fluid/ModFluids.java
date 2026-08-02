package com.shand1an.sreln.fluid;

import com.shand1an.sreln.srelnMod;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, srelnMod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, srelnMod.MODID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(srelnMod.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(srelnMod.MODID);

    public static final DeferredHolder<FluidType, ChemicalToxicFluidType> CHEMICAL_TOXIC_FLUID_TYPE =
            FLUID_TYPES.register("chemical_toxic_fluid", ChemicalToxicFluidType::new);

    public static final DeferredHolder<Fluid, ChemicalToxicFluid.Source> CHEMICAL_TOXIC_FLUID_SOURCE =
            FLUIDS.register("chemical_toxic_fluid", ChemicalToxicFluid.Source::new);

    public static final DeferredHolder<Fluid, ChemicalToxicFluid.Flowing> CHEMICAL_TOXIC_FLUID_FLOWING =
            FLUIDS.register("chemical_toxic_fluid_flowing", ChemicalToxicFluid.Flowing::new);

    public static final DeferredBlock<ChemicalToxicLiquidBlock> CHEMICAL_TOXIC_LIQUID_BLOCK =
            BLOCKS.register("chemical_toxic_fluid", () -> new ChemicalToxicLiquidBlock(
                    CHEMICAL_TOXIC_FLUID_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredItem<BucketItem> CHEMICAL_TOXIC_BUCKET =
            ITEMS.register("chemical_toxic_bucket", () -> new BucketItem(
                    CHEMICAL_TOXIC_FLUID_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredHolder<FluidType, CrudeOilFluidType> CRUDE_OIL_FLUID_TYPE =
            FLUID_TYPES.register("crude_oil_fluid", CrudeOilFluidType::new);

    public static final DeferredHolder<Fluid, CrudeOilFluid.Source> CRUDE_OIL_FLUID_SOURCE =
            FLUIDS.register("crude_oil_fluid", CrudeOilFluid.Source::new);

    public static final DeferredHolder<Fluid, CrudeOilFluid.Flowing> CRUDE_OIL_FLUID_FLOWING =
            FLUIDS.register("crude_oil_fluid_flowing", CrudeOilFluid.Flowing::new);

    public static final DeferredBlock<CrudeOilLiquidBlock> CRUDE_OIL_LIQUID_BLOCK =
            BLOCKS.register("crude_oil_fluid", () -> new CrudeOilLiquidBlock(
                    CRUDE_OIL_FLUID_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredItem<BucketItem> CRUDE_OIL_BUCKET =
            ITEMS.register("crude_oil_bucket", () -> new BucketItem(
                    CRUDE_OIL_FLUID_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
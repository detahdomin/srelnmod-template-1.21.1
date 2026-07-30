package com.shand1an.sreln.block;

import com.shand1an.sreln.block.corpse.CorpseBlock;
import com.shand1an.sreln.block.corpse.CorpseBlockItem;
import com.shand1an.sreln.block.terminal.TerminalBlock;
import com.shand1an.sreln.item.ModItems;
import com.shand1an.sreln.srelnMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(srelnMod.MODID);

    public static final DeferredBlock<Block> ORBITAL_STRIKE_CANNON =
            registerBlock("orbital_strike_cannon", () -> new OrbitalStrikeCannonBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)));

    public static final DeferredBlock<Block> WHITE_POOL_TILE =
            registerBlock("white_pool_tile", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)));

    public static final DeferredBlock<Block> SALVATION_RESEARCH_INSTITUTE_LIGHT =
            registerBlock("salvation_research_institute_light", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).noOcclusion().lightLevel(s -> 15)));

    public static final DeferredBlock<Block> WHITE_CERAMIC_TILE_PILLAR =
            registerBlock("white_ceramic_tile_pillar", () -> new WhiteCeramicTilePillarBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).noOcclusion()));

    public static final DeferredBlock<Block> LIGHTING_CONSOLE =
            registerBlock("lighting_console", () -> new LightingConsoleBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).noOcclusion()));

    public static final DeferredBlock<TerminalBlock> TERMINAL =
            registerBlock("terminal", () -> new TerminalBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).noOcclusion()));

    public static final DeferredBlock<CorpseBlock> CORPSE_HANGING_MALE =
            registerCorpseBlock("corpse_hanging_male", "hanging_male");

    public static final DeferredBlock<CorpseBlock> CORPSE_BEHEADED_MALE =
            registerCorpseBlock("corpse_beheaded_male", "beheaded_male");

    public static final DeferredBlock<CorpseBlock> CORPSE_DISMEMBERED_FEMALE =
            registerCorpseBlock("corpse_dismembered_female", "dismembered_female");

    public static final DeferredBlock<CorpseBlock> CORPSE_DISMEMBERED_MALE =
            registerCorpseBlock("corpse_dismembered_male", "dismembered_male");

    public static final DeferredBlock<CorpseBlock> CORPSE_BROKEN_FEMALE =
            registerCorpseBlock("corpse_broken_female", "broken_female");

    public static final DeferredBlock<CorpseBlock> CORPSE_CRUSHED_HEAD_MALE =
            registerCorpseBlock("corpse_crushed_head_male", "crushed_head_male");

    private static DeferredBlock<CorpseBlock> registerCorpseBlock(String name, String variant) {
        DeferredBlock<CorpseBlock> block = BLOCKS.register(name,
                () -> new CorpseBlock(variant, BlockBehaviour.Properties.of().strength(0.5F).noOcclusion().sound(SoundType.WOOL)));
        registerCorpseBlockItem(name, block);
        return block;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static void registerCorpseBlockItem(String name, DeferredBlock<CorpseBlock> block) {
        ModItems.ITEMS.register(name, () -> new CorpseBlockItem(block.get(), new Item.Properties(), block.get().variant));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
        registerBlockItem(name, blocks);
        return  blocks;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
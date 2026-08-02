package com.shand1an.sreln.item;

import com.shand1an.sreln.srelnMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(srelnMod.MODID);

    public static final DeferredItem<Item> Lingering_Ender_Pearl =
            ITEMS.register("pearl/lingering_ender_pearl", () -> new LingeringEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Harming_Ender_Pearl =
            ITEMS.register("pearl/harming_ender_pearl", () -> new HarmingEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Recall_Ender_Pearl =
            ITEMS.register("pearl/recall_ender_pearl", () -> new RecallEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Swap_Ender_Pearl =
            ITEMS.register("pearl/swap_ender_pearl", () -> new SwapEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Splinter_Ender_Pearl =
            ITEMS.register("pearl/splinter_ender_pearl", () -> new SplinterEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Skyward_Ender_Pearl =
            ITEMS.register("pearl/skyward_ender_pearl", () -> new SkywardEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Ricochet_Ender_Pearl =
            ITEMS.register("pearl/ricochet_ender_pearl", () -> new RicochetEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Echo_Ender_Pearl =
            ITEMS.register("pearl/echo_ender_pearl", () -> new EchoEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Swift_Ender_Pearl =
            ITEMS.register("pearl/swift_ender_pearl", () -> new SwiftEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> Assault_Ender_Pearl =
            ITEMS.register("pearl/assault_ender_pearl", () -> new AssaultEnderPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> TARGET_DESIGNATOR =
            ITEMS.register("target_designator", () -> new TargetDesignatorItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> MIMIC =
            ITEMS.register("mimic", () -> new MimicItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> TRAVELER_KEY =
            ITEMS.register("traveler_key", () -> new TravelerKeyItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> TERMINAL_BINDER =
            ITEMS.register("terminal_binder", () -> new TerminalBinderItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FACILITY_BINDER =
            ITEMS.register("facility_binder", () -> new FacilityBinderItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> HACKER_BADGE =
            ITEMS.register("hacker_badge", () -> new HackerBadgeItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
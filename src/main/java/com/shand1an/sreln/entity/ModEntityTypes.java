package com.shand1an.sreln.entity;

import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, srelnMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RecallEnderPearlEntity>> RECALL_ENDER_PEARL =
            ENTITY_TYPES.register("recall_ender_pearl",
                    () -> EntityType.Builder.<RecallEnderPearlEntity>of(RecallEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("recall_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<LingeringEnderPearlEntity>> LINGERING_ENDER_PEARL =
            ENTITY_TYPES.register("lingering_ender_pearl",
                    () -> EntityType.Builder.<LingeringEnderPearlEntity>of(LingeringEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("lingering_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<FakePlayerEntity>> FAKE_PLAYER =
            ENTITY_TYPES.register("fake_player",
                    () -> EntityType.Builder.<FakePlayerEntity>of(FakePlayerEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("fake_player"));

    public static final DeferredHolder<EntityType<?>, EntityType<HarmingEnderPearlEntity>> HARMING_ENDER_PEARL =
            ENTITY_TYPES.register("harming_ender_pearl",
                    () -> EntityType.Builder.<HarmingEnderPearlEntity>of(HarmingEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("harming_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<SwapEnderPearlEntity>> SWAP_ENDER_PEARL =
            ENTITY_TYPES.register("swap_ender_pearl",
                    () -> EntityType.Builder.<SwapEnderPearlEntity>of(SwapEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("swap_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<SplinterEnderPearlEntity>> SPLINTER_ENDER_PEARL =
            ENTITY_TYPES.register("splinter_ender_pearl",
                    () -> EntityType.Builder.<SplinterEnderPearlEntity>of(SplinterEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("splinter_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<SkywardEnderPearlEntity>> SKYWARD_ENDER_PEARL =
            ENTITY_TYPES.register("skyward_ender_pearl",
                    () -> EntityType.Builder.<SkywardEnderPearlEntity>of(SkywardEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("skyward_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<RicochetEnderPearlEntity>> RICOCHET_ENDER_PEARL =
            ENTITY_TYPES.register("ricochet_ender_pearl",
                    () -> EntityType.Builder.<RicochetEnderPearlEntity>of(RicochetEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("ricochet_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<EchoEnderPearlEntity>> ECHO_ENDER_PEARL =
            ENTITY_TYPES.register("echo_ender_pearl",
                    () -> EntityType.Builder.<EchoEnderPearlEntity>of(EchoEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("echo_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<SummonedWarden>> SUMMONED_WARDEN =
            ENTITY_TYPES.register("summoned_warden",
                    () -> EntityType.Builder.<SummonedWarden>of(SummonedWarden::new, MobCategory.MONSTER)
                            .sized(0.9F, 2.9F)
                            .clientTrackingRange(16)
                            .fireImmune()
                            .build("summoned_warden"));

    public static final DeferredHolder<EntityType<?>, EntityType<SwiftEnderPearlEntity>> SWIFT_ENDER_PEARL =
            ENTITY_TYPES.register("swift_ender_pearl",
                    () -> EntityType.Builder.<SwiftEnderPearlEntity>of(SwiftEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("swift_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<AssaultEnderPearlEntity>> ASSAULT_ENDER_PEARL =
            ENTITY_TYPES.register("assault_ender_pearl",
                    () -> EntityType.Builder.<AssaultEnderPearlEntity>of(AssaultEnderPearlEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("assault_ender_pearl"));

    public static final DeferredHolder<EntityType<?>, EntityType<TargetDesignatorEntity>> TARGET_DESIGNATOR =
            ENTITY_TYPES.register("target_designator",
                    () -> EntityType.Builder.<TargetDesignatorEntity>of(TargetDesignatorEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(3)
                            .build("target_designator"));

    public static final DeferredHolder<EntityType<?>, EntityType<OrbitalLaserEntity>> ORBITAL_LASER =
            ENTITY_TYPES.register("orbital_laser",
                    () -> EntityType.Builder.<OrbitalLaserEntity>of(OrbitalLaserEntity::new, MobCategory.MISC)
                            .sized(0.5F, 320F)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .noSummon()
                            .build("orbital_laser"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
package com.shand1an.sreln.sound;

import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, srelnMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> JIGUANG_FASHE =
            SOUND_EVENTS.register("jiguangfashe",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "jiguangfashe")));

    public static final DeferredHolder<SoundEvent, SoundEvent> JIGUANG_CHIXU =
            SOUND_EVENTS.register("jiguangchixu",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "jiguangchixu")));

    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSMISSION_SOUND =
            SOUND_EVENTS.register("transmission_sound_effect",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "transmission_sound_effect")));

    public static final DeferredHolder<SoundEvent, SoundEvent> QIDONG_JIGUANG =
            SOUND_EVENTS.register("qidongjiguang",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "qidongjiguang")));

    public static final DeferredHolder<SoundEvent, SoundEvent> POWER_BUTTON =
            SOUND_EVENTS.register("power_button",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "power_button")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FAN_HUM =
            SOUND_EVENTS.register("fan_hum",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "fan_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WELCOME_VOICE =
            SOUND_EVENTS.register("welcome_voice",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "welcome_voice")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SHUTDOWN_VOICE =
            SOUND_EVENTS.register("shutdown_voice",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "shutdown_voice")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
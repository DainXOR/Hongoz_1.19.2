package net.dain.hongozmod.sound;

import net.dain.hongozmod.HongozMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HongozMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ZHONGO_AMBIENT = registerSoundEvent("zhongo_idle");
    public static final RegistryObject<SoundEvent> ZHONGO_HURT = registerSoundEvent("zhongo_hurt");
    public static final RegistryObject<SoundEvent> ZHONGO_DEATH = registerSoundEvent("zhongo_death");

    //public static final ForgeSoundType TEST_SOUNDS = new ForgeSoundType(1.0f, 1.0f, ModSounds.TEST_SOUND_1, ModSounds.TEST_SOUND_2, ModSounds.TEST_SOUND_3,);

    public static RegistryObject<SoundEvent> registerSoundEvent(String name){
        ResourceLocation id = new ResourceLocation(HongozMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> new SoundEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}


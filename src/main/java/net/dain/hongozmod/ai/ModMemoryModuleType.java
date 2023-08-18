package net.dain.hongozmod.ai;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
/*
public class ModMemoryModuleType<U>  {
    private final Optional<Codec<ExpirableValue<U>>> codec;

    @VisibleForTesting
    public ModMemoryModuleType(Optional<Codec<U>> pOptionalCodec) {
        this.codec = pOptionalCodec.map(ExpirableValue::codec);
    }

    public @NotNull String toString() {
        //return Registry.MEMORY_MODULE_TYPE.getKey(this).toString();
        return Registry.MEMORY_MODULE_TYPE_REGISTRY.toString();
    }

    public @NotNull Optional<Codec<ExpirableValue<U>>> getCodec() {
        return this.codec;
    }

    private static <U> ModMemoryModuleType<U> register(String pIdentifier, Codec<U> pCodec) {
        Registry.registerMapping(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(pIdentifier), new MemoryModuleType<>(Optional.of(pCodec)));
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(pIdentifier), new MemoryModuleType<>(Optional.of(pCodec)));
    }

    private static <U> ModMemoryModuleType<U> register(String pIdentifier) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, new ResourceLocation(pIdentifier), new ModMemoryModuleType<>(Optional.empty()));
    }
}
*/
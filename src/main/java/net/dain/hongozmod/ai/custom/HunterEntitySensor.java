package net.dain.hongozmod.ai.custom;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class HunterEntitySensor extends NearestLivingEntitySensor<HunterEntity> {
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    protected void doTick(@NotNull ServerLevel pLevel, @NotNull HunterEntity pEntity) {
        super.doTick(pLevel, pEntity);
        getClosest(pEntity, (entity) -> {
            return entity.getType() == EntityType.PLAYER;
        }).or(() -> {
            return getClosest(pEntity, (entity) -> {
                return entity.getType() != EntityType.PLAYER;
            });
        }).ifPresentOrElse((entity) -> {
            pEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, entity);
        }, () -> {
            pEntity.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        });
    }

    private static Optional<LivingEntity> getClosest(HunterEntity hunter, Predicate<LivingEntity> pPredicate) {
        return hunter.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(hunter::canTargetEntity).filter(pPredicate).findFirst();
    }

    protected int radiusXZ() {
        return 24;
    }

    protected int radiusY() {
        return 24;
    }
}

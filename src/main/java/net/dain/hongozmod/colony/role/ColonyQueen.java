package net.dain.hongozmod.colony.role;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface ColonyQueen extends ColonyMember {
    boolean requestAdoption(@NotNull ColonyMember entity);

    int getWorkerChance();
    int getExplorerChance();
    int getWarriorChance();
    int getRoyalWarriorChance();

    int getHeirChance();
    int getFoodStoreChance();
    float getRejectChance();

    int getBirthAmount();

    int getMinBirthCooldown();
    int getBirthCooldown();

    LivingEntity getTarget();
    double getAlertRange();

    void callProtection();
    void callColony();
    void forceCallColony();

    default boolean isQueen() {
        return true;
    }
}

package net.dain.hongozmod.colony.role;

import net.dain.hongozmod.colony.AlertLevel;
import net.dain.hongozmod.colony.Colony;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public interface ColonyMember {

    <ColonyType extends Colony> boolean setColony(@NotNull ColonyType newColony);
    <ColonyType extends Colony> ColonyType getColony();

    default ColonyQueen getQueen(){
        return this.getColony().getQueen();
    }

    void setRoles(EnumSet<ColonyRoles> newRoles);
    void addRole(ColonyRoles role);
    @NotNull EnumSet<ColonyRoles> getRoles();
    <NewRoleClass extends ColonyMember> NewRoleClass changeRole(ColonyRoles newRole);

    void returnToQueen(@NotNull AlertLevel priority);

    void alertColony();

    boolean isHeir();
    boolean canBecomeQueen();
    int queeningScore();
    ColonyQueen becomeQueen();
    default boolean isQueen(){ return false; }
}

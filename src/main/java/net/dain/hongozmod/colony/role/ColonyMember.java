package net.dain.hongozmod.colony.role;

import net.dain.hongozmod.colony.AlertLevel;
import net.dain.hongozmod.colony.Colony;
import org.jetbrains.annotations.NotNull;

public interface ColonyMember {

    boolean setColony(Colony newCOlony);
    Colony getColony();

    default ColonyQueen getQueen(){
        return (ColonyQueen) this.getColony().getQueen();
    }

    void setRole(ColonyRoles role);
    ColonyRoles getRole();
    <NewRoleClass extends ColonyMember> NewRoleClass changeRole(ColonyRoles newRole);
    void returnToQueen(@NotNull AlertLevel priority);

    void alertColony();

    default boolean isQueen(){ return false; }
}

package net.dain.hongozmod.colony.role;

public interface ColonyHeir extends ColonyMember {

    boolean canBecomeQueen();
    ColonyQueen becomeQueen();

    int queeningScore();
}

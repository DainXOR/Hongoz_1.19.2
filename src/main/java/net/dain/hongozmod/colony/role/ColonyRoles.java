package net.dain.hongozmod.colony.role;

public enum ColonyRoles {
    NONE(           0b1),
    WORKER(         0b1 << 1),
    EXPLORER(       0b1 << 2),
    WARRIOR(        0b1 << 3),
    ROYAL_WARRIOR(  0b1 << 4),
    QUEEN(          0b1 << 5),

    FOOD_SACK(      0b1 << 6),
    HEIR(           0b1 << 7);

    public final int value;

    ColonyRoles(int value){
        this.value = value;
    }
    
}

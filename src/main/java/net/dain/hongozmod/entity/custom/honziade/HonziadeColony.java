package net.dain.hongozmod.entity.custom.honziade;

import net.dain.hongozmod.colony.Colony;

public final class HonziadeColony extends Colony<AbstractHonziadeEntity, HonziadeQueen, HonziadeColony> {

    HonziadeColony(HonziadeQueen newQueen){
        super(HonziadeColony.class, newQueen);
    }

}

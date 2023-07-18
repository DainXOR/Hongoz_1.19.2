package net.dain.hongozmod.entity.custom;

import net.dain.hongozmod.entity.templates.Infected;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class TestMob extends Infected {
    private static final String entityName = "test_mob";

    protected TestMob(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }
}

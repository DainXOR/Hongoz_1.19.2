package net.dain.hongozmod.entity.custom.honziade;

import net.dain.hongozmod.colony.AlertLevel;
import net.dain.hongozmod.colony.role.ColonyQueen;
import net.dain.hongozmod.colony.role.ColonyWorker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

public class HonziadeWorker extends AbstractHonziadeEntity implements ColonyWorker {
    public HonziadeWorker(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    public static AttributeSupplier setAttributes(){
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.00)
                .add(Attributes.ATTACK_DAMAGE, 1.00)
                .add(Attributes.ATTACK_SPEED, 2.00)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.FOLLOW_RANGE, 128.00)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.50)
                .build();
    }

    @Override
    public void returnToQueen(@NotNull AlertLevel priority) {

    }

    @Override
    public void alertColony() {

    }

    @Override
    public boolean isHeir() {
        return false;
    }

    @Override
    public boolean canBecomeQueen() {
        return false;
    }

    @Override
    public ColonyQueen becomeQueen() {
        return null;
    }

    @Override
    public int queeningScore() {
        return 0;
    }
}
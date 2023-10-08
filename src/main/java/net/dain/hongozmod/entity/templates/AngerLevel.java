package net.dain.hongozmod.entity.templates;

import net.minecraft.world.entity.monster.Monster;

import java.util.function.Predicate;

public enum AngerLevel {
    BASE(0, (entity) -> true),
    INVESTIGATE(10, (entity) -> true),
    PURSUIT(20, (entity) -> true),
    DESPERATE(35, (entity) -> entity.getHealth() < entity.getMaxHealth()),
    ANGRY(50, (entity) -> true),
    ENRAGED(100, (entity) -> entity.getHealth() <= entity.getMaxHealth() * 0.7f),
    BERSERK(70, (entity) -> entity.getHealth() <= entity.getMaxHealth() * 0.5f && entity.getTarget() == null);

    private final int value;
    private final Predicate<Monster> entityCondition;

    AngerLevel(int minAnger, Predicate<Monster> condition){
        this.value = minAnger;
        this.entityCondition = condition;
    }

    public final int getValue(){
        return this.value;
    }
    public static AngerLevel getLevel(int anger, Monster entity){
        AngerLevel level = getLevelByValue(anger);

        while (!level.testCondition(entity)) {
            level = AngerLevel.values()[level.ordinal() - 1];
        }

        return level;
    }
    public static AngerLevel getLevelByValue(int anger){
        if (anger != 0){
            for (AngerLevel level : AngerLevel.values()) {
                if (level.getValue() > anger){
                    return AngerLevel.values()[level.ordinal() - 1];
                }
            }
        }

        return BASE;
    }

    public final Boolean testCondition(Monster entity){
        return entityCondition.test(entity);
    }
    public final Boolean is(int value){
        return value >= this.value && value <= AngerLevel.values()[this.ordinal() + 1].value;
    }
    public final Boolean equalsOrAbove(int value){
        return value >= this.value;
    }
    public final Boolean equalsOrAbove(AngerLevel level){
        return level.ordinal() >= this.ordinal();
    }
}

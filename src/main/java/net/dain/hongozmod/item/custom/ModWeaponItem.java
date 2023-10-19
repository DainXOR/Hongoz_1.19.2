package net.dain.hongozmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dain.hongozmod.tags.ModBlockTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Vanishable;

import javax.annotation.Nullable;
import java.util.List;

public class ModWeaponItem extends ModToolItem {
    private float attackDamage;
    private float attackSpeed;
    private float attackReach;

    private Multimap<Attribute, AttributeModifier> defaultModifiers;

    private ModWeaponItem(Tier tier, float damageModifier, float attackSpeedModifier, float attackReachModifier, Properties properties) {
        super(tier, damageModifier, attackSpeedModifier, List.of(ModBlockTags.MINEABLE_WITH_SWORD), properties);
        this.attackReach = attackReachModifier;

    }

    public static class Builder {
        private ModWeaponItem item;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder;

        public ModWeaponItem.Builder create(Tier tier, float damageModifier, float attackSpeedModifier, float attackReachModifier, Properties properties){
            this.item = new ModWeaponItem(tier, damageModifier, attackSpeedModifier, attackReachModifier, properties);
            builder = ImmutableMultimap.builder();
            return this;
        }

        public ModWeaponItem.Builder setAttackDamageModifier(int amount, @Nullable AttributeModifier.Operation modifierOperation){
            this.item.attackDamage = amount + this.item.getTier().getAttackDamageBonus();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID,
                    "Weapon modifier",
                    this.item.attackDamage,
                    modifierOperation != null ? modifierOperation : AttributeModifier.Operation.ADDITION));
            return this;
        }
        public ModWeaponItem.Builder setAttackSpeedModifier(int amount, @Nullable AttributeModifier.Operation modifierOperation){
            this.item.attackSpeed = amount;
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                    BASE_ATTACK_SPEED_UUID,
                    "Weapon modifier",
                    this.item.attackSpeed,
                    modifierOperation != null ? modifierOperation : AttributeModifier.Operation.ADDITION));
            return this;
        }
        public ModWeaponItem.Builder setAttackReachModifier(int amount){
            this.item.attackReach = amount;
            return this;
        }

        public ModWeaponItem.Builder addAttribute(Attribute attribute, String name, double amount, @Nullable AttributeModifier.Operation operation){
            this.item.defaultModifiers.put(attribute, new AttributeModifier(name, amount, operation != null ? operation : AttributeModifier.Operation.ADDITION));
            return this;
        }

        public ModWeaponItem build(){

            this.item.defaultModifiers = builder.build();
            return this.item;
        }
    }


}

package net.dain.hongozmod.material;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;

public interface Material {

    String getName();

    // float getHardness();    // How stiff
    float getStrength();    // How heavy before failure (plastic deformation)
    float getDuctility();   // Plastic deformation before rupture
    // float getToughness();   // Strength and ductility crossover
    // int getDurability();
    float getDensity();

    int getEnchantmentValue();
    boolean isFireResistant();

    Ingredient getRepairIngredient();



}

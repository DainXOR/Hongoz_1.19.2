package net.dain.hongozmod.material;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.AnvilUpdateEvent;

import java.util.EnumMap;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    LEATHER("leather", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)),
    CHAIN("chainmail", ModTiers.IRON, new int[]{1, 4, 5, 2}, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F),
    IRON(ModTiers.IRON, new int[]{2, 5, 6, 2}, SoundEvents.ARMOR_EQUIP_IRON, 0.0F),
    GOLD(ModTiers.GOLD, new int[]{1, 3, 5, 2}, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F),
    DIAMOND(ModTiers.DIAMOND, new int[]{3, 6, 8, 3}, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F),
    TURTLE("turtle", 25, new int[]{2, 5, 6, 2}, 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.SCUTE)),
    NETHERITE(ModTiers.NETHERITE, new int[]{3, 6, 8, 3}, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F),
    WOLFRAMIUM(ModTiers.WOLFRAMIUM, new int[]{5, 9, 12, 4}, SoundEvents.ARMOR_EQUIP_IRON, 5.0F);

    private static final EnumMap<EquipmentSlot, Integer> BASE_USES = Util.make(new EnumMap<>(EquipmentSlot.class), (values) -> {
        values.put(EquipmentSlot.FEET, 13);
        values.put(EquipmentSlot.LEGS, 15);
        values.put(EquipmentSlot.CHEST, 16);
        values.put(EquipmentSlot.HEAD, 11);
    });
    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<EquipmentSlot, Integer> protections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ModArmorMaterials(ModTiers tier, int[] protectionPerPiece, SoundEvent equipSound, float toughness){
        this(
                tier,
                Util.make(new EnumMap<EquipmentSlot, Integer>(EquipmentSlot.class), (values) -> {
                            values.put(EquipmentSlot.FEET, protectionPerPiece[0]);
                            values.put(EquipmentSlot.LEGS, protectionPerPiece[1]);
                            values.put(EquipmentSlot.CHEST, protectionPerPiece[2]);
                            values.put(EquipmentSlot.HEAD, protectionPerPiece[3]);
                        }),
                equipSound,
                toughness);
    }
    ModArmorMaterials(String name, ModTiers tier, int[] protectionPerPiece, SoundEvent equipSound, float toughness){
        this(
                name,
                tier,
                Util.make(new EnumMap<EquipmentSlot, Integer>(EquipmentSlot.class), (values) -> {
                    values.put(EquipmentSlot.FEET, protectionPerPiece[0]);
                    values.put(EquipmentSlot.LEGS, protectionPerPiece[1]);
                    values.put(EquipmentSlot.CHEST, protectionPerPiece[2]);
                    values.put(EquipmentSlot.HEAD, protectionPerPiece[3]);
                }),
                equipSound,
                toughness);
    }
    ModArmorMaterials(ModTiers tier, EnumMap<EquipmentSlot, Integer> protectionPerPiece, SoundEvent equipSound, float toughness){
        this.name = tier.name().toLowerCase();
        this.durabilityMultiplier = tier.getUses() / 4;
        this.protections = protectionPerPiece;
        this.enchantmentValue = tier.getEnchantmentValue();
        this.sound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = tier.getDensity() <= 100 ? tier.getDensity() / 100 : 1;
        this.repairIngredient = new LazyLoadedValue<>(tier::getRepairIngredient);


    }
    ModArmorMaterials(String name, ModTiers tier, EnumMap<EquipmentSlot, Integer> protectionPerPiece, SoundEvent equipSound, float toughness){
        this.name = name;
        this.durabilityMultiplier = tier.getUses() / 4;
        this.protections = protectionPerPiece;
        this.enchantmentValue = tier.getEnchantmentValue();
        this.sound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = tier.getDensity() <= 100 ? tier.getDensity() / 100 : 1;
        this.repairIngredient = new LazyLoadedValue<>(tier::getRepairIngredient);


    }

    ModArmorMaterials(String name,
                      int durabilityMultiplier,
                      int[] protectionPerPiece,
                      int enchantmentValue,
                      SoundEvent sound,
                      float toughness,
                      float knockbackResistance,
                      Supplier<Ingredient> repairIngredient){
        this(
                name,
                durabilityMultiplier,
                Util.make(new EnumMap<EquipmentSlot, Integer>(EquipmentSlot.class), (values) -> {
                    values.put(EquipmentSlot.FEET, protectionPerPiece[0]);
                    values.put(EquipmentSlot.LEGS, protectionPerPiece[1]);
                    values.put(EquipmentSlot.CHEST, protectionPerPiece[2]);
                    values.put(EquipmentSlot.HEAD, protectionPerPiece[3]);
                }),
                enchantmentValue,
                sound,
                toughness,
                knockbackResistance,
                repairIngredient);
    }
    ModArmorMaterials(String name,
                      int durabilityMultiplier,
                      EnumMap<EquipmentSlot, Integer> protectionPerPiece,
                      int enchantmentValue,
                      SoundEvent sound,
                      float toughness,
                      float knockbackResistance,
                      Supplier<Ingredient> repairIngredient){
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protections = protectionPerPiece;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot pSlot) {
        return 0;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot pSlot) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public SoundEvent getEquipSound() {
        return null;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

}

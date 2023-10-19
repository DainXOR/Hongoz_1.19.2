package net.dain.hongozmod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public enum ModTiers implements Tier {
    WOOD(Tiers.WOOD, 0.7f, false),
    STONE(Tiers.STONE, 2.5f, false),
    IRON(Tiers.IRON, 7.9f, false),
    DIAMOND(Tiers.DIAMOND, 3.5f, false),
    GOLD(Tiers.GOLD, 19.3f, false),
    NETHERITE(Tiers.NETHERITE, (GOLD.getDensity() * 4) + 3.2f /* Mafic lava */, true),
    WOLFRAMIUM(6, 1 << 12, 11.0f, 19.25f, 9.0f, true, 32, () -> Ingredient.of(ModItems.WOLFRAMIUM_INGOT.get()));

    private final int level;
    private final int uses;
    private final float speed;
    private final float density;
    private final float damage;
    private final boolean fireResistant;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    /**
     * @param level                Mining level.
     * @param uses                 Base tool / armor uses.
     * @param speed                Mining speed. Check {@link Tiers} for reference.
     * @param density              Density of the material (grams / cubic meters).
     * @param damage               Base damage.
     * @param enchantmentValue     The "enchantability" of the material. Check {@link Tiers} for reference.
     * @param repairIngredient     Material used to repair items with this tier.
     */
    private ModTiers(int level, int uses, float speed, float density, float damage, boolean fireResistant, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.density = density;
        this.damage = damage;
        this.fireResistant = fireResistant;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }
    private ModTiers(Tier tier, float density, boolean fireResistant){
        this(
                tier.getLevel(),
                tier.getUses(),
                tier.getSpeed(),
                density,
                tier.getAttackDamageBonus(),
                fireResistant,
                tier.getEnchantmentValue(),
                tier::getRepairIngredient);
    }

    public int getLevel() {
        return this.level;
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float affectAttackSpeed(float baseSpeed){
        return (baseSpeed - 4) * (1 + (this.getDensity() / 100));
    }

    public float getDensity() {
        return this.density;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public boolean isFireResistant() {
        return this.fireResistant;
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Nullable
    public TagKey<Block> getTag(ModTiers tier) {
        return switch(tier) {
            case WOOD -> Tags.Blocks.NEEDS_WOOD_TOOL;
            case GOLD -> Tags.Blocks.NEEDS_GOLD_TOOL;
            case STONE -> BlockTags.NEEDS_STONE_TOOL;
            case IRON -> BlockTags.NEEDS_IRON_TOOL;
            case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
            case NETHERITE -> Tags.Blocks.NEEDS_NETHERITE_TOOL;
            case WOLFRAMIUM -> ModTags.Blocks.NEEDS_WOLFRAMIUM_TOOL;
        };
    }
}

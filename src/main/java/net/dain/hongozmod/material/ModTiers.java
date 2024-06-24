package net.dain.hongozmod.material;

import net.dain.hongozmod.item.ModItems;
import net.dain.hongozmod.tags.ModBlockTags;
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
    WOOD(0f, Tiers.WOOD, 0.7f, false),
    STONE(6f, Tiers.STONE, 2.5f, false),
    IRON(4f, Tiers.IRON, 7.9f, false),
    DIAMOND(10f, Tiers.DIAMOND, 3.5f, false),
    GOLD(2.5f, Tiers.GOLD, 19.3f, false),
    NETHERITE(7f, Tiers.NETHERITE, (GOLD.getDensity() * 3) + 3.2f /* Mafic lava */, true),

    WOLFRAMIUM(7.5f, 2, 1827, 8.0f, 19.25f, 9.0f, true, 32, () -> Ingredient.of(ModItems.WOLFRAMIUM_INGOT.get())),
    WOLFRAMIUM_CARBIDE(9f, 3, 4096, 10.0f, 19.25f, 9.0f, true, 32, () -> Ingredient.of(ModItems.WOLFRAMIUM_CARBIDE_INGOT.get()));

    private final float hardness;
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
     * @param hardness             Wiwilsi.
     * @param uses                 Base tool / armor uses.
     * @param speed                Mining speed. Check {@link Tiers} for reference.
     * @param density              Density of the material (grams / cubic meters).
     * @param damage               Base damage.
     * @param enchantmentValue     The "enchantability" of the material. Check {@link Tiers} for reference.
     * @param repairIngredient     Material used to repair items with this tier.
     */
    ModTiers(float hardness, int level, int uses, float speed, float density, float damage, boolean fireResistant, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.hardness = hardness;
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.density = density;
        this.damage = damage;
        this.fireResistant = fireResistant;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }
    ModTiers(float hardness, Tier tier, float density, boolean fireResistant){
        this(
                hardness,
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

    public float getHardness(){return this.hardness; }

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
            case WOLFRAMIUM, WOLFRAMIUM_CARBIDE -> ModBlockTags.NEEDS_WOLFRAMIUM_TOOL;
        };
    }
}

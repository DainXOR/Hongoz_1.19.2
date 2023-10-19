package net.dain.hongozmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dain.hongozmod.item.ModTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModToolItem extends DiggerItem {
    /** Hardcoded set of blocks this tool can properly dig at full speed. Modders see instead. */
    protected final float attackDamage;
    protected final float attackSpeed;
    protected final float digSpeed;
    protected final List<TagKey<Block>> blocks;
    protected final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ModToolItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, List<TagKey<Block>> mineableTag, Properties properties) {
        super(attackDamageModifier, attackSpeedModifier, tier, mineableTag.get(0),
                tier instanceof ModTiers modTier && modTier.isFireResistant() ? properties.fireResistant() : properties);

        this.attackDamage = attackDamageModifier + tier.getAttackDamageBonus();
        this.digSpeed = tier.getSpeed();
        this.blocks = mineableTag;

        if(tier instanceof ModTiers modTier){
            this.attackSpeed = modTier.affectAttackSpeed(attackSpeedModifier);
        }
        else {
            this.attackSpeed = attackSpeedModifier;
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", this.attackSpeed, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();

    }

    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return this.blocks.stream().anyMatch(state::is) ? this.speed : 1.0f;
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, (p_41007_) -> {
            p_41007_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    /**
     * Called when a {@link net.minecraft.world.level.block.Block} is destroyed using this Item. Return {@code true} to
     * trigger the "Use Item" statistic.
     */
    public boolean mineBlock(@NotNull ItemStack pStack,
                             @NotNull Level pLevel,
                             @NotNull BlockState pState,
                             @NotNull BlockPos pPos,
                             @NotNull LivingEntity pEntityLiving) {
        if (!pLevel.isClientSide && pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
            pStack.hurtAndBreak(1, pEntityLiving, (p_40992_) -> {
                p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }

    public float getAttackDamage() {
        return this.attackDamage;
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    @Deprecated // FORGE: Use stack sensitive variant below
    public boolean isCorrectToolForDrops(@NotNull BlockState state) {
        if (TierSortingRegistry.isTierSorted(getTier())) {
            return TierSortingRegistry.isCorrectTierForDrops(getTier(), state) && this.blocks.stream().anyMatch(state::is);
        }
        int i = this.getTier().getLevel();
        if (i < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (i < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return (i >= 1 || !state.is(BlockTags.NEEDS_STONE_TOOL)) && this.blocks.stream().anyMatch(state::is);
        }
    }

    // FORGE START
    @Override
    public boolean isCorrectToolForDrops(@NotNull ItemStack stack, BlockState state) {
        return this.blocks.stream().anyMatch(state::is) && TierSortingRegistry.isCorrectTierForDrops(getTier(), state);
    }

}

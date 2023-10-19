package net.dain.hongozmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class ModShieldItem extends TieredItem implements Vanishable {
    public static final int EFFECTIVE_BLOCK_DELAY = 10;
    public static final float MINIMUM_DURABILITY_DAMAGE = 1.0f;
    public static final String TAG_BASE_COLOR = "Base";

    protected final int durability;
    protected Multimap<Attribute, AttributeModifier> defaultModifiers;
    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder;

    public ModShieldItem(Tier tier, Properties properties) {
        super(tier, properties);

        this.durability = tier.getUses() << 2;

        this.builder = ImmutableMultimap.builder();
        this.builder.put(Attributes.ARMOR, new AttributeModifier("Shield modifier", 2.5f, AttributeModifier.Operation.ADDITION));
        this.builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier("Shield modifier", 1.0f, AttributeModifier.Operation.ADDITION));
        this.builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier("Shield modifier", 0.5f, AttributeModifier.Operation.ADDITION));
        this.builder.put(Attributes.MAX_HEALTH, new AttributeModifier("Shield modifier", 6.0f, AttributeModifier.Operation.ADDITION));
        this.builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier("Shield modifier", -0.03f, AttributeModifier.Operation.ADDITION));

        this.defaultModifiers = builder.build();
    }

    public ModShieldItem addAttribute(Attribute attribute, double amount, @Nullable AttributeModifier.Operation operation){
        this.builder.put(attribute, new AttributeModifier(
                "Shield modifier",
                amount,
                operation != null ? operation : AttributeModifier.Operation.ADDITION));

        this.defaultModifiers = builder.build();
        return this;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND ?
                this.defaultModifiers : super.getAttributeModifiers(slot, stack);
    }



    public @NotNull String getDescriptionId(@NotNull ItemStack pStack) {
        return BlockItem.getBlockEntityData(pStack) != null ? this.getDescriptionId() + "." + getColor(pStack).getName() : super.getDescriptionId(pStack);
    }

    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    public int getUseDuration(@NotNull ItemStack pStack) {
        return this.durability;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack ingredient) {
        return super.isValidRepairItem(toRepair, ingredient);
    }

    public static DyeColor getColor(ItemStack pStack) {
        return DyeColor.GRAY;
    }

    /* ******************** FORGE START ******************** */

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }

}

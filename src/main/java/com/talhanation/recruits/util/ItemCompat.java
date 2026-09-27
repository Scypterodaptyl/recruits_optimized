package com.talhanation.recruits.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

import javax.annotation.Nullable;

/**
 * Replacements for the item class checks (SwordItem, ArmorItem, DiggerItem, ...) that
 * were removed in favour of data components.
 */
public final class ItemCompat {

    private ItemCompat() {
    }

    public static boolean isSword(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.SWORDS);
    }

    public static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.AXES);
    }

    public static boolean isDigger(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES));
    }

    public static boolean isMeleeWeapon(ItemStack stack) {
        return isSword(stack) || isDigger(stack) || stack.is(ItemTags.SPEARS) || stack.has(DataComponents.WEAPON);
    }

    @Nullable
    public static Equippable getEquippable(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(DataComponents.EQUIPPABLE);
    }

    public static boolean isArmor(ItemStack stack) {
        Equippable equippable = getEquippable(stack);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    @Nullable
    public static EquipmentSlot getArmorSlot(ItemStack stack) {
        Equippable equippable = getEquippable(stack);
        return equippable != null ? equippable.slot() : null;
    }

    @Nullable
    public static Holder<SoundEvent> getEquipSound(ItemStack stack) {
        Equippable equippable = getEquippable(stack);
        return equippable != null ? equippable.equipSound() : null;
    }

    public static double getAttributeValue(ItemStack stack, Holder<Attribute> attribute) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double value = 0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(attribute)) {
                value += entry.modifier().amount();
            }
        }
        return value;
    }

    public static double getAttackDamage(ItemStack stack) {
        return getAttributeValue(stack, Attributes.ATTACK_DAMAGE);
    }

    public static double getArmorDefense(ItemStack stack) {
        return getAttributeValue(stack, Attributes.ARMOR);
    }

    public static double getArmorToughness(ItemStack stack) {
        return getAttributeValue(stack, Attributes.ARMOR_TOUGHNESS);
    }

    public static boolean isEdible(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD);
    }

    public static void setCrossbowCharged(ItemStack crossbow, boolean charged) {
        if (charged) {
            if (!CrossbowItem.isCharged(crossbow)) {
                crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
            }
        } else {
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    public static Iterable<MobEffectInstance> getPotionEffects(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
    }

    public static boolean hasOnlyBeneficialEffects(ItemStack stack) {
        boolean any = false;
        for (MobEffectInstance instance : getPotionEffects(stack)) {
            any = true;
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) return false;
        }
        return any;
    }
}

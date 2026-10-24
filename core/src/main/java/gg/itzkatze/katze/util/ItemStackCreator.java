package gg.itzkatze.katze.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for creating {@link ItemStack} objects with various customizations,
 * including item materials, names, lore, and player skins.
 */
public class ItemStackCreator {

    /**
     * Creates an {@link ItemStack} with a specified material and custom name.
     *
     * @param material the material of the item stack
     * @param name     the custom name of the item stack
     * @return a new {@link ItemStack} with the specified properties
     */
    public static ItemStack createNamedItemStack(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(withoutDefaultItalics(name));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack fillerItem() {
        return createNamedItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
    }

    /**
     * Creates an {@link ItemStack} with a custom name and lore list.
     *
     * @param name     the display name
     * @param material the item material
     * @param amount   the stack amount
     * @param lore     a list of lore lines
     * @return a new {@link ItemStack} with the specified name and lore
     */
    public static ItemStack getStack(Component name, Material material, int amount, List<Component> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(withoutDefaultItalics(name));
            meta.lore(lore.stream().map(ItemStackCreator::withoutDefaultItalics).collect(Collectors.toList()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an {@link ItemStack} with a custom name and lore list.
     *
     * @param name     the display name
     * @param material the item material
     * @param lore     a list of lore lines
     * @return a new {@link ItemStack} with the specified name and lore
     */
    public static ItemStack getStack(Component name, Material material, List<Component> lore) {
        return getStack(name, material, 1, lore);
    }

    /**
     * Creates an {@link ItemStack} with a name and multiple lore lines.
     *
     * @param name     the display name
     * @param material the item material
     * @param amount   the stack amount
     * @param lore     one or more lore lines
     * @return a new {@link ItemStack} with the specified name and lore
     */
    public static ItemStack getStack(Component name, Material material, int amount, Component... lore) {
        return getStack(name, material, amount, Arrays.asList(lore));
    }

    /**
     * Applies an enchantment glint to the given {@link ItemStack}.
     *
     * @param item the {@link ItemStack} to modifiy
     * @return the same {@link ItemStack} with a glint effect
     */
    public static ItemStack enchant(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a textured skull item from a predefined {@link Skull} enum constant.
     *
     * @param skull   the {@link Skull} enum value
     * @param name    the display name
     * @param amount  the stack amount
     * @param lore    the lore lines
     * @return a new {@link ItemStack} representing the skull
     */
    public static ItemStack getSkull(Skull skull, Component name, int amount, List<Component> lore) {
        ItemStack item = skull.asItemStack().clone();
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(withoutDefaultItalics(name));
            meta.lore(lore.stream().map(ItemStackCreator::withoutDefaultItalics).collect(Collectors.toList()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a skull item from a {@link Skull} enum with varargs lore.
     *
     * @param skull   the skull type
     * @param name    the display name
     * @param amount  the stack amount
     * @param lore    one or more lore lines
     * @return a new {@link ItemStack} representing the skull
     */
    public static ItemStack getSkull(Skull skull, Component name, int amount, Component... lore) {
        return getSkull(skull, name, amount, Arrays.asList(lore));
    }

    /**
     * Creates a player head from a player's {@link UUID}.
     *
     * @param uuid    the player's unique ID
     * @param name    the display name
     * @param amount  the stack amount
     * @param lore    one or more lore lines
     * @return a new {@link ItemStack} representing the player's head
     */
    public static ItemStack getPlayerSkull(UUID uuid, Component name, int amount, Component... lore) {
        ItemStack skull = Skull.PlayerSkulls.getSkullByUUID(uuid);
        skull.setAmount(amount);

        ItemMeta meta = skull.getItemMeta();
        if (meta != null) {
            meta.customName(withoutDefaultItalics(name));
            meta.lore(Arrays.stream(lore).map(ItemStackCreator::withoutDefaultItalics).collect(Collectors.toList()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            skull.setItemMeta(meta);
        }

        return skull;
    }

    private static Component withoutDefaultItalics(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static boolean hasData(ItemStack stack, NamespacedKey key) {
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    public static ItemStack createSpecialItem(
            Material material,
            Component name,
            List<Component> lore,
            NamespacedKey key,
            int amount
    ) {
        ItemStack item = ItemStackCreator.getStack(name, material, amount, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        }
        return item;
    }
}

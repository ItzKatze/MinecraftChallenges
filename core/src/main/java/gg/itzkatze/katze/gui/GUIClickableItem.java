package gg.itzkatze.katze.gui;

import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.Skull;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GUIClickableItem extends GUIItem {

    public GUIClickableItem(final int slot) {
        super(slot);
    }

    public abstract void run(InventoryClickEvent event, KatzePlayer player);

    public static GUIClickableItem getCloseItem(int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent e, KatzePlayer player) {
                if (player.getPlayer() == null) return;
                player.getPlayer().closeInventory();
            }

            @Override
            public ItemStack getItem(KatzePlayer player) {
                return ItemStackCreator.createNamedItemStack(
                        Material.BARRIER,
                        Component.text("Close").color(KatzeColor.RED)
                );
            }
        };
    }

    public static GUIClickableItem getGoBackItem(int slot, KatzeGUI gui) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent e, KatzePlayer player) {
                gui.open(player);
            }

            @Override
            public ItemStack getItem(KatzePlayer player) {
                return ItemStackCreator.getSkull(
                        Skull.ARROW_LEFT,
                        Component.text("Go Back").color(KatzeColor.LIME),
                        1,
                        Component.text("To " + gui.getTitle()).color(KatzeColor.LIGHT_GRAY)
                );
            }
        };
    }

    public static GUIClickableItem getGoForthItem(int slot, KatzeGUI gui) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent e, KatzePlayer player) {
                gui.open(player);
            }

            @Override
            public ItemStack getItem(KatzePlayer player) {
                return ItemStackCreator.getSkull(
                        Skull.ARROW_RIGHT,
                        Component.text("Go Forth").color(KatzeColor.LIME),
                        1,
                        Component.text("To " + gui.getTitle()).color(KatzeColor.LIGHT_GRAY)
                );
            }
        };
    }

    @Override
    public void onClick(InventoryClickEvent event, KatzePlayer player) {
        run(event, player);
    }

}

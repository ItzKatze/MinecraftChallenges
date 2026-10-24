package gg.itzkatze.katze.gui;

import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GUIItem {
    public final int slot;
    public abstract ItemStack getItem(KatzePlayer player);

    public GUIItem(int slot) {
        this.slot = slot;
    }

    public void onClick(InventoryClickEvent event, KatzePlayer player) {
    }
}

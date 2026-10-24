package gg.itzkatze.katze.gui;

import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KatzeGUI gui)) return;
        event.setCancelled(!gui.allowItemMoving() || isSpectating(event.getWhoClicked()));

        if (event.getRawSlot() < 0) return;
        if (event.getRawSlot() >= gui.getSize()) return;

        GUIItem clicked = gui.get(event.getRawSlot());
        if (clicked == null) return;

        if (clicked instanceof GUIClickableItem clickable) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            KatzePlayer katzePlayer = KatzePlayer.adapt(player);

            clickable.onClick(event, katzePlayer);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KatzeGUI gui) {
            event.setCancelled(!gui.allowItemMoving() || isSpectating(event.getWhoClicked()));
        }
    }

    // Paper closes the old screen only after KatzeGUI.open registered the new one, so only a mapping to the closed screen is removed.
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof KatzeGUI gui) {
            gui.onClose(event, KatzeGUI.CloseReason.PLAYER_EXITED);
            KatzeGUI.GUI_MAP.remove(event.getPlayer().getUniqueId(), gui);
        }
    }

    // Paper hands spectator-mode clicks over already cancelled and performs them when a listener un-cancels them.
    private static boolean isSpectating(HumanEntity who) {
        return who.getGameMode() == GameMode.SPECTATOR;
    }
}

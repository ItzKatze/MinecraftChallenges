package gg.itzkatze.mcc.lobby.cosmetics;

import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class MusicPackListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ResourcePackRequest request = MusicPack.get().request();
        if (request != null) {
            event.getPlayer().sendResourcePacks(request);
        }
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        if (!MusicPack.PACK_ID.equals(event.getID())) return;
        Player player = event.getPlayer();
        MusicPack.get().status(player.getUniqueId(), player.getName(), event.getStatus());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MusicPack.get().forget(event.getPlayer().getUniqueId());
    }
}

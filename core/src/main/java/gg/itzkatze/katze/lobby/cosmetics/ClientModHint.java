package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public final class ClientModHint implements Listener {

    private static final long CHECK_DELAY_TICKS = 100;
    private static final String DOWNLOAD_URL = "https://modrinth.com/mod/katzegamesmod/versions";
    private static final Component HINT = Component.text(
            "You're playing without the KatzeGames client mod. Install it from Modrinth for an overall better experience! ", KatzeColor.YELLOW)
            .append(Component.text("[Download]", KatzeColor.LIME).clickEvent(ClickEvent.openUrl(DOWNLOAD_URL)));

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(KatzeGames.get(), () -> {
            if (player.isConnected() && !MusicChannel.isListening(player)) {
                player.sendMessage(HINT);
            }
        }, CHECK_DELAY_TICKS);
    }
}

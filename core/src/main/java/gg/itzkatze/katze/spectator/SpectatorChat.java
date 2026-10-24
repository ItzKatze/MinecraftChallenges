package gg.itzkatze.katze.spectator;

import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelTag;
import gg.itzkatze.katze.util.KatzeColor;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class SpectatorChat implements Listener {

    // Before TeamChatManager, so a spectator's leftover team chat toggle can't reach a team.
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(@NotNull AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (!SpectatorManager.isSpectator(sender.getUniqueId()) || !SpectatorManager.isBattleRunning()) return;

        event.setCancelled(true);
        Component line = Component.textOfChildren(
                Component.text("[Spectator] ", KatzeColor.LIGHT_GRAY),
                LevelTag.of(LevelStore.get().level(sender.getUniqueId())),
                Component.text(" <" + sender.getName() + "> ", KatzeColor.WHITE),
                event.message().colorIfAbsent(KatzeColor.WHITE));
        // The viewers are the snapshot Paper built for this message, which keeps this async thread off the wrapper list. They include the console.
        for (Audience viewer : event.viewers()) {
            if (viewer instanceof Player player && SpectatorManager.isSpectator(player.getUniqueId())) {
                player.sendMessage(line);
            }
        }
        Bukkit.getConsoleSender().sendMessage(line);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        if (!SpectatorManager.isSpectator(sender.getUniqueId()) || !SpectatorManager.isBattleRunning()) return;

        Component refusal = WhisperFilter.refusal(event.getMessage(), SpectatorChat::isOnlineSpectator);
        if (refusal != null) {
            event.setCancelled(true);
            sender.sendMessage(refusal);
        }
    }

    private static boolean isOnlineSpectator(@NotNull String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && SpectatorManager.isSpectator(player.getUniqueId());
    }
}

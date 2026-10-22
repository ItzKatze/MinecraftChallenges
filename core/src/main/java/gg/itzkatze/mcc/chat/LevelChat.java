package gg.itzkatze.mcc.chat;

import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.level.LevelTag;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class LevelChat implements Listener {

    // After SpectatorChat and TeamChatManager took their messages; runs on the chat thread, so it only reads the level cache.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(@NotNull AsyncChatEvent event) {
        event.renderer((source, displayName, message, viewer) -> Component.textOfChildren(
                LevelTag.of(LevelStore.get().level(source.getUniqueId())),
                Component.text(" <"),
                displayName,
                Component.text("> "),
                message));
    }
}

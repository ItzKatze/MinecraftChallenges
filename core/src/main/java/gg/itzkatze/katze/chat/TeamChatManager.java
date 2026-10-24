package gg.itzkatze.katze.chat;

import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelTag;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.team.KatzeTeam;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class TeamChatManager implements Listener {

    private static final Set<UUID> TEAM_CHAT_PLAYERS = new HashSet<>();

    public static boolean isTeamChatEnabled(@NotNull KatzePlayer player) {
        return TEAM_CHAT_PLAYERS.contains(player.getUuid());
    }

    public static void setTeamChatEnabled(@NotNull KatzePlayer player, boolean enabled) {
        if (enabled) {
            TEAM_CHAT_PLAYERS.add(player.getUuid());
        } else {
            TEAM_CHAT_PLAYERS.remove(player.getUuid());
        }
    }

    public static void sendTeamMessage(@NotNull KatzePlayer sender, @NotNull Component message) {
        KatzeTeam team = sender.getTeam();
        if (team == null) {
            sender.sendMessage(Component.text("You are not currently in a team.", NamedTextColor.RED));
            return;
        }

        Component formattedMessage = Component.text("[Team] ", NamedTextColor.AQUA)
                .append(LevelTag.of(LevelStore.get().level(sender.getUuid())))
                .append(Component.text(" <" + sender.getName() + "> ", NamedTextColor.WHITE))
                .append(message);
        team.getPlayers().forEach(member -> member.sendMessage(formattedMessage));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(@NotNull AsyncChatEvent event) {
        Player sender = event.getPlayer();
        KatzePlayer katzePlayer = KatzePlayer.adapt(sender);
        if (!isTeamChatEnabled(katzePlayer)) {
            return;
        }

        event.setCancelled(true);
        sendTeamMessage(katzePlayer, event.message());
    }
}

package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyManager;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LobbyPosCommand extends KatzeCommand {

    public LobbyPosCommand() {
        super(
                "lobbypos",
                List.of(),
                CallerType.PLAYER,
                "/lobbypos",
                PermissionDefault.OP,
                "Command to show your lobby island position for placing NPCs"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (!(caller instanceof KatzePlayer katzePlayer) || katzePlayer.getPlayer() == null) return;
        Player player = katzePlayer.getPlayer();

        Lobby lobby = LobbyManager.getActiveLobby();
        if (lobby == null) {
            caller.sendMessage(Component.text("No lobby is active.").color(NamedTextColor.RED));
            return;
        }
        if (!player.getWorld().equals(lobby.getWorld())) {
            caller.sendMessage(Component.text("You are not in the lobby.").color(NamedTextColor.RED));
            return;
        }

        Location location = player.getLocation();
        int yaw = snapYaw(lobby.toStructureYaw(location.getYaw()));
        caller.sendMessage(positionMessage(lobby.getIsland().getId(), lobby.toStructure(location), yaw));
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }

    static int snapYaw(float structureYaw) {
        return (int) Location.normalizeYaw(Math.round(structureYaw / 5f) * 5f);
    }

    static @NotNull Component positionMessage(@NotNull String islandId, @NotNull Vector block, int yaw) {
        double x = block.getBlockX() + 0.5;
        double y = block.getBlockY();
        double z = block.getBlockZ() + 0.5;

        String arguments = "new Vector(" + x + ", " + y + ", " + z + "), " + yaw;

        return Component.text("Island " + islandId + ": " + x + " " + y + " " + z + ", yaw " + yaw + " ", NamedTextColor.YELLOW)
                .append(Component.text("[Copy]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.copyToClipboard(arguments))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy"))));
    }
}

package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.lobby.Lobby;
import gg.itzkatze.katze.lobby.LobbyIsland;
import gg.itzkatze.katze.lobby.LobbyManager;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SetSpawnCommand extends KatzeCommand {

    public SetSpawnCommand() {
        super(
                "setspawn",
                List.of(),
                CallerType.NONE,
                "/setspawn [island]",
                PermissionDefault.OP,
                "Command to switch the lobby to another island"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (args.length > 1) {
            sendUsage(caller);
            return;
        }
        Lobby lobby = LobbyManager.getActiveLobby();
        if (lobby == null) {
            fail(caller, "No lobby is active.");
            return;
        }

        LobbyIsland target = args.length == 0 ? LobbyManager.pickOtherIsland(ThreadLocalRandom.current()) : findIsland(args[0]);
        if (target == null) {
            fail(caller, args.length == 0
                    ? "There is no other island to switch to."
                    : "Unknown island \"" + args[0] + "\". Available islands: " + String.join(", ", islandIds()) + ".");
            return;
        }
        if (target.getId().equals(lobby.getIsland().getId())) {
            fail(caller, "Island " + target.getId() + " is already active.");
            return;
        }
        if (!LobbyManager.switchIsland(target)) {
            fail(caller, "Island " + target.getId() + " could not be loaded, the lobby was not changed.");
            return;
        }
        caller.sendMessage(Component.text("Switched the lobby island to " + target.getId() + ".").color(NamedTextColor.GREEN));
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        Lobby lobby = LobbyManager.getActiveLobby();
        if (lobby == null || args.length != 1) {
            return List.of();
        }
        String current = lobby.getIsland().getId();
        List<String> others = islandIds().stream().filter(id -> !id.equals(current)).toList();
        return StringUtility.copyPartialMatches(args[0], others, new ArrayList<>());
    }

    private static @Nullable LobbyIsland findIsland(@NotNull String id) {
        return LobbyManager.getIslands().stream().filter(island -> island.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private static @NotNull List<String> islandIds() {
        return LobbyManager.getIslands().stream().map(LobbyIsland::getId).toList();
    }

    private static void fail(@NotNull CommandCaller caller, @NotNull String message) {
        caller.sendMessage(Component.text(message).color(NamedTextColor.RED));
    }
}

package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.chat.TeamChatManager;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.spectator.SpectatorManager;
import gg.itzkatze.katze.spectator.WhisperFilter;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class TeamChatCommand extends KatzeCommand {

    public TeamChatCommand() {
        super(
                "t",
                List.of(),
                CallerType.PLAYER,
                "/t chat <message>",
                PermissionDefault.TRUE,
                "Send a message directly to team chat"
        );
    }

    @Override
    protected void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("chat")) {
            sendUsage(caller);
            return;
        }

        KatzePlayer player = KatzePlayer.adapt(caller.getName()).orElse(null);
        if (player == null) return;
        if (player.isSpectator() && SpectatorManager.isBattleRunning()) {
            caller.sendMessage(WhisperFilter.ONLY_SPECTATOR_CHAT);
            return;
        }
        TeamChatManager.sendTeamMessage(player, Component.text(String.join(" ", args).substring(5)));
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        if (args.length == 1 && "chat".startsWith(args[0].toLowerCase())) {
            return List.of("chat");
        }
        return List.of();
    }
}

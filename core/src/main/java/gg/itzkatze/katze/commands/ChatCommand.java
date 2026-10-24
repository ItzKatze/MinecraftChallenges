package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.chat.TeamChatManager;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class ChatCommand extends KatzeCommand {

    public ChatCommand() {
        super(
                "chat",
                List.of(),
                CallerType.PLAYER,
                "/chat <team|all>",
                PermissionDefault.TRUE,
                "Switch between team chat and normal chat"
        );
    }

    @Override
    protected void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (args.length != 1) {
            sendUsage(caller);
            return;
        }

        KatzePlayer player = KatzePlayer.adapt(caller.getName()).orElse(null);
        if (player == null) {
            return;
        }

        switch (args[0].toLowerCase()) {
            case "team" -> {
                if (!player.isInTeam()) {
                    caller.sendMessage(Component.text("You are not currently in a team.", NamedTextColor.RED));
                    return;
                }
                TeamChatManager.setTeamChatEnabled(player, true);
                caller.sendMessage(Component.text("You are now talking in team chat.", NamedTextColor.GREEN));
            }
            case "all" -> {
                TeamChatManager.setTeamChatEnabled(player, false);
                caller.sendMessage(Component.text("You are now talking in normal chat.", NamedTextColor.GREEN));
            }
            default -> sendUsage(caller);
        }
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("team", "all"), new ArrayList<>());
        }
        return List.of();
    }
}

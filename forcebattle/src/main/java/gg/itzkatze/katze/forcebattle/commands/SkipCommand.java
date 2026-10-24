package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.TabCompletions;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkipCommand extends KatzeCommand {

    public SkipCommand() {
        super(
                "skip",
                List.of(),
                CallerType.NONE,
                "/skip <player/team> <player/team>",
                PermissionDefault.OP,
                "Command to skip a player's objective - meant for admins if an item is unobtainable"
        );
    }

    @Override
    public void executeCommand(CommandCaller caller, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "player" -> {
                    @NotNull Optional<KatzePlayer> battlePlayerOpt = KatzePlayer.adapt(args[1]);
                    battlePlayerOpt.ifPresentOrElse(
                            battlePlayer -> {
                                if (battlePlayer.isOffline()) {
                                    return;
                                }
                                battlePlayer.updateObjective(false, false);
                                battlePlayer.sendMessage(Component.text("Your objective has been skipped by an admin.").color(KatzeColor.LIME));
                                caller.sendMessage(Component.text("Successfully skipped the objective of " + battlePlayer.getName() + ".").color(KatzeColor.LIME));
                            }, () -> caller.sendMessage(Component.text("Player couldn't be found.").color(KatzeColor.RED))
                    );
                }
                case "team" -> {
                    try {
                        @NotNull Optional<KatzeTeam> teamOpt = KatzeTeam.ofId(Integer.parseInt(args[1]));
                        teamOpt.ifPresentOrElse(
                                team -> {
                                    team.updateObjective(null, false, false);
                                    team.getPlayers().forEach(player -> player.sendMessage(Component.text("Your objective has been skipped by an admin.").color(KatzeColor.LIME)));
                                    caller.sendMessage(Component.text("Successfully skipped the objective of " + team.getId() + ".").color(KatzeColor.LIME));
                                }, () -> caller.sendMessage(Component.text("Team couldn't be found.").color(KatzeColor.RED))
                        );
                    } catch (NumberFormatException e) {
                        caller.sendMessage(Component.text("Please specify a valid number.").color(KatzeColor.RED));
                    }
                }
            }
        } else {
            sendUsage(caller);
        }
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("player", "team"), new ArrayList<>());
        } else if (args.length == 2) {
            return switch (args[0]) {
                case "player" -> StringUtility.copyPartialMatches(args[1], TabCompletions.getPlayerNamesTabCompletions(), new ArrayList<>());
                case "team" -> StringUtility.copyPartialMatches(args[1], TabCompletions.getTeamIdTabCompletions(), new ArrayList<>());
                default -> List.of();
            };
        }
        return List.of();
    }

}

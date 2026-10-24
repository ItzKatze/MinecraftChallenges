package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.TabCompletions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamCommand extends KatzeCommand {

    public TeamCommand() {
        super(
                "team",
                List.of(),
                CallerType.PLAYER,
                "/team <create|join|leave|list|accept|reject> <id>",
                PermissionDefault.TRUE,
                "Command to manage teams: create, join, etc."
        );
    }

    @Override
    public void executeCommand(CommandCaller caller, String[] args) {
        if (args.length < 1) {
            sendUsage(caller);
            return;
        }

        @NotNull Optional<KatzePlayer> battlePlayerOpt = KatzePlayer.adapt(caller.getName());
        if (battlePlayerOpt.isEmpty()) {
            return;
        }
        KatzePlayer katzePlayer = battlePlayerOpt.get();

        if (!args[0].equalsIgnoreCase("list") && ChallengeManager.isGameStarted()) {
            caller.sendMessage(Component.text("Teams cannot be changed until the game is reset.").color(NamedTextColor.RED));
            return;
        }
        if ((args[0].equals("create") || args[0].equals("join")) && katzePlayer.isSpectator()) {
            caller.sendMessage(Component.text("Spectators can't join teams. Type /spectate to play.").color(NamedTextColor.RED));
            return;
        }

        switch (args[0]) {
            case "create" -> {
                katzePlayer.leaveTeam();
                KatzeTeam team = KatzeTeam.create(List.of(katzePlayer));
                caller.sendMessage(Component.text("New team has been created. Team ID: " + team.getId()));
            }
            case "join" -> {
                if (args.length < 2) {
                    caller.sendMessage(Component.text("Please specify a valid team ID.").color(NamedTextColor.RED));
                    return;
                }
                int teamId;
                try {
                    teamId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    caller.sendMessage(Component.text("Please specify a valid team ID.").color(NamedTextColor.RED));
                    return;
                }

                Optional<KatzeTeam> teamOpt = KatzeTeam.ofId(teamId);
                teamOpt.ifPresentOrElse(
                        team -> team.sendJoinRequest(katzePlayer),
                        () -> caller.sendMessage(Component.text("Please specify a valid team ID.").color(NamedTextColor.RED))
                );
            }
            case "leave" -> {
                katzePlayer.leaveTeam();
            }
            case "accept" -> {
                KatzeTeam team = katzePlayer.getTeam();
                if (team == null) {
                    caller.sendMessage(Component.text("You are not currently in a team.").color(NamedTextColor.GRAY));
                    return;
                }
                if (team.getLastJoinRequest() != null) {
                    KatzePlayer requester = team.getLastJoinRequest().katzePlayer();
                    if (requester.isSpectator()) {
                        caller.sendMessage(Component.text(requester.getName() + " is spectating and can't join teams.").color(NamedTextColor.RED));
                        team.finishLastJoinRequest(false);
                        return;
                    }
                    team.finishLastJoinRequest(true);
                }
            }
            case "reject" -> {
                KatzeTeam team = katzePlayer.getTeam();
                if (team == null) {
                    caller.sendMessage(Component.text("You are not currently in a team.").color(NamedTextColor.GRAY));
                    return;
                }
                if (team.getLastJoinRequest() != null) {
                    team.finishLastJoinRequest(false);
                }
            }
            case "list" -> {
                Component teamList = Component.text("Teams:", NamedTextColor.BLUE).decorate(TextDecoration.BOLD);
                for (KatzeTeam team : KatzeTeam.teams) {
                    teamList = teamList.append(Component.text("\n"))
                            .append(Component.text(team.getId() + ":", NamedTextColor.YELLOW));
                    List<KatzePlayer> teamPlayers = team.getPlayers();
                    for (int i = 0; i < teamPlayers.size(); i++) {
                        KatzePlayer teamPlayer = teamPlayers.get(i);
                        if (teamPlayer == null || teamPlayer.isOffline()) {
                            continue;
                        }
                        teamList = teamList.append(Component.text(" " + teamPlayer.getName(), NamedTextColor.BLUE));
                        if (i < teamPlayers.size() - 1) {
                            teamList = teamList.append(Component.text(",", NamedTextColor.BLUE));
                        }
                    }
                }

                caller.sendMessage(teamList);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("create", "list", "join", "leave", "accept", "reject"), new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return StringUtility.copyPartialMatches(args[1], TabCompletions.getTeamIdTabCompletions(), new ArrayList<>());
        }
        return List.of();
    }

}

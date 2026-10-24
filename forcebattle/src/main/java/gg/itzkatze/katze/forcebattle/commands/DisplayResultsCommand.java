package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DisplayResultsCommand extends KatzeCommand {

    public DisplayResultsCommand() {
        super(
                "displayresults",
                List.of(),
                CallerType.NONE,
                "/displayresults <player/team>",
                PermissionDefault.OP,
                "Command to display the results of the battle"
        );
    }

    @Override
    protected void executeCommand(CommandCaller caller, String[] args) {
        if (!KatzeGames.getTimer().hasHitZero()) {
            caller.sendMessage(Component.text("The challenge is not yet over.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendUsage(caller);
            return;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final long delay = 750;
        final long[] counter = {0};

        switch (args[0].toLowerCase()) {
            case "player" -> {
                List<KatzePlayer> sortedPlayers = new ArrayList<>(KatzePlayer.PLAYERS.stream().filter(KatzePlayer::isRanked).toList());
                sortedPlayers.sort(Comparator.comparingInt(KatzePlayer::getPoints).reversed());

                for (KatzePlayer player : sortedPlayers) {
                    int place = KatzePlayer.getPlace(player);
                    int points = player.getPoints();

                    scheduler.schedule(() -> KatzeGames.broadcast(
                            Component.text(place + ". ", NamedTextColor.GOLD)
                                    .append(Component.text(player.getName(), NamedTextColor.BLUE))
                                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text(points, NamedTextColor.GOLD))
                    ), counter[0] * delay, TimeUnit.MILLISECONDS);
                    counter[0]++;
                }
            }

            case "team" -> {
                List<KatzeTeam> sortedTeams = new ArrayList<>(KatzeTeam.teams);
                sortedTeams.sort(Comparator.comparingInt(KatzeTeam::getPoints).reversed());

                for (KatzeTeam team : sortedTeams) {
                    int place = KatzeTeam.getPlace(team);
                    int points = team.getPoints();

                    String players = team.getPlayers().stream()
                            .sorted(Comparator.comparingInt(KatzePlayer::getPoints).reversed())
                            .map(p -> p.getName() + " (" + p.getPoints() + ")")
                            .collect(Collectors.joining(", "));

                    scheduler.schedule(() -> KatzeGames.broadcast(
                            Component.text(place + ". ", NamedTextColor.GOLD)
                                    .append(Component.text(String.valueOf(team.getId()), NamedTextColor.BLUE))
                                    .append(Component.text(" (", NamedTextColor.BLUE))
                                    .append(Component.text(players, NamedTextColor.GRAY))
                                    .append(Component.text(")", NamedTextColor.BLUE))
                                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text(points, NamedTextColor.GOLD))
                    ), counter[0] * delay, TimeUnit.MILLISECONDS);
                    counter[0]++;
                }
            }

            default -> sendUsage(caller);
        }

        scheduler.shutdown();
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("player", "team"), new ArrayList<>());
        }
        return List.of();
    }
}

package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.commands.framework.CallerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTeamsCommand extends KatzeCommand {

    public RandomTeamsCommand() {
        super(
                "randomteams",
                List.of(),
                CallerType.PLAYER,
                "/randomteams <teamsize> <mode>",
                PermissionDefault.OP,
                "Command to create random teams."
        );
    }

    @Override
    protected void executeCommand(final CommandCaller caller, final String[] args) {
        if (KatzeGames.getTimer().isRunning()) {
            caller.sendMessage(Component.text("The game has already started.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0 || args[0].isEmpty()) {
            sendUsage(caller);
            return;
        }

        int size;
        try {
            size = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            caller.sendMessage(Component.text("Please specify a valid number.").color(NamedTextColor.RED));
            return;
        }

        List<KatzePlayer> players = new ArrayList<>(KatzePlayer.getOnlinePlayers().stream().filter(player -> !player.isSpectator()).toList());
        int playerAmount = players.size();

        if (playerAmount % size != 0) {
            caller.sendMessage(Component.text("No equal teams can be created.").color(NamedTextColor.RED));
            return;
        }

        int amountOfTeams = playerAmount / size;
        Random random = new Random();

        if (args.length > 1 && args[1].equalsIgnoreCase("fillExistingTeams")) {
            List<KatzeTeam> existingTeams = new ArrayList<>(KatzeTeam.teams);

            for (KatzeTeam team : existingTeams) {
                for (KatzePlayer p : team.getPlayers()) {
                    players.remove(p);
                }
            }

            for (KatzeTeam team : existingTeams) {
                while (team.getPlayers().size() > size) {
                    KatzePlayer removed = team.getPlayers().get(random.nextInt(team.getPlayers().size()));
                    team.removePlayer(removed);
                    players.add(removed);
                }
            }

            for (KatzeTeam team : existingTeams) {
                while (team.getPlayers().size() < size && !players.isEmpty()) {
                    KatzePlayer randomPlayer = players.remove(random.nextInt(players.size()));
                    team.addPlayer(randomPlayer);
                }
            }

            while (!players.isEmpty()) {
                KatzePlayer first = players.remove(random.nextInt(players.size()));
                KatzeTeam team = KatzeTeam.create(List.of(first));

                while (team.getPlayers().size() < size && !players.isEmpty()) {
                    KatzePlayer next = players.remove(random.nextInt(players.size()));
                    team.addPlayer(next);
                }
            }

        } else {
            KatzeTeam.deleteAll();

            for (int i = 0; i < amountOfTeams; i++) {
                KatzePlayer first = players.remove(random.nextInt(players.size()));
                KatzeTeam team = KatzeTeam.create(List.of(first));

                while (team.getPlayers().size() < size && !players.isEmpty()) {
                    KatzePlayer next = players.remove(random.nextInt(players.size()));
                    team.addPlayer(next);
                }
            }
        }

        caller.sendMessage(Component.text("Successfully created " + amountOfTeams + " Teams.").color(NamedTextColor.GREEN));
    }

    @Override
    protected List<String> tabComplete(final CommandCaller caller, final String[] args) {
        if (args.length == 2) {
            return StringUtility.copyPartialMatches(args[1], List.of("fillExistingTeams"), new ArrayList<>());
        }
        return List.of();
    }
}

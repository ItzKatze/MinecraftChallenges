package gg.itzkatze.katze.util;

import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Comparator;
import java.util.stream.Collectors;

public class ResultUtils {

    public static Component buildPlayerResultMessage(KatzePlayer player) {
        int place = KatzePlayer.getPlace(player);

        return Component.text("Results → ")
                .append(Component.text(player.getName()).color(KatzeColor.GOLD))
                .append(Component.text(" | Place: " + place + " | Points: " + player.getPoints())
                        .color(KatzeColor.YELLOW))
                .append(Component.text(" [Open Results]")
                        .color(KatzeColor.LIME)
                        .clickEvent(ClickEvent.runCommand("/result player " + player.getName()))
                );
    }

    public static Component buildTeamResultMessage(KatzeTeam team) {
        int place = KatzeTeam.getPlace(team);
        String players = team.getPlayers().stream()
                .sorted(Comparator.comparingInt(KatzePlayer::getPoints).reversed())
                .map(p -> p.getName() + " (" + p.getPoints() + ")")
                .collect(Collectors.joining(", "));

        return Component.text("Results → ")
                .append(Component.text("Team " + team.getId()).color(KatzeColor.GOLD))
                .append(Component.text(" [" + players + "] ")).color(KatzeColor.LIGHT_GRAY)
                .append(Component.text(" | Place: " + place + " | Points: " + team.getPoints())
                        .color(KatzeColor.YELLOW))
                .append(Component.text(" [Open Results]")
                        .color(KatzeColor.LIME)
                        .clickEvent(ClickEvent.runCommand("/result team " + team.getId())));
    }
}

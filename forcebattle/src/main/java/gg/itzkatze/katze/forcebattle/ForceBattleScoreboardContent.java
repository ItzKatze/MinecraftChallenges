package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.scoreboard.ScoreboardContent;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ForceBattleScoreboardContent implements ScoreboardContent {

    private static final int MAX_STRING_LENGTH = 30;

    @Override
    @NotNull public Component getTitle() {
        return Component.text("ForceBattle");
    }

    @Override
    @NotNull public Component[] getLines(@NotNull KatzePlayer katzePlayer) {
        ForceBattlePlayer player = (ForceBattlePlayer) katzePlayer;
        List<Component> lines = new ArrayList<>();

        // Player's objective
        ForceBattleObjective selfObjective = player.getObjective();
        if (selfObjective != null) {
            lines.add(Component.text("Your Objective:").color(NamedTextColor.YELLOW));
            String objName = getObjectiveDisplay(selfObjective);
            lines.add(Component.text(truncate(objName)).color(NamedTextColor.WHITE));
        } else {
            lines.add(Component.text("No current objective").color(NamedTextColor.GRAY));
        }

        lines.add(Component.text(" ")); // Spacer

        // Team information
        if (player.isInTeam()) {
            KatzeTeam team = player.getTeam();

            // Team objective (if enabled)
            ForceBattleObjective teamObjective = (ForceBattleObjective) team.getObjective();
            if (teamObjective != null && SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE)) {
                lines.add(Component.text("Team Objective:").color(NamedTextColor.YELLOW));
                String objName = getObjectiveDisplay(teamObjective);
                lines.add(Component.text(truncate(objName)).color(NamedTextColor.WHITE));
                lines.add(Component.text("  ")); // Spacer
            }

            // Teammates
            if (team.getPlayers().size() > 1) {
                lines.add(Component.text("Teammates:").color(NamedTextColor.YELLOW));

                for (KatzePlayer teammate : team.getPlayers()) {
                    if (teammate.equals(player)) continue;
                    ForceBattlePlayer teammatePlayer = (ForceBattlePlayer) teammate;

                    lines.add(Component.text("* ").color(NamedTextColor.GRAY)
                            .append(Component.text(teammate.getName()).color(NamedTextColor.AQUA)));

                    ForceBattleObjective teammateObjective = teammatePlayer.getObjective();
                    String objName = teammateObjective != null
                            ? getObjectiveDisplay(teammateObjective)
                            : "None";

                    lines.add(Component.text("  " + truncate(objName)).color(NamedTextColor.WHITE));
                }
            }
        }

        return lines.toArray(new Component[0]);
    }

    private String getObjectiveDisplay(ForceBattleObjective objective) {
        String icon = switch (objective.getBattleType()) {
            case FORCE_ITEM -> "✦";
            case FORCE_MOB -> "☮";
            case FORCE_ADVANCEMENT -> "★";
            case FORCE_HEIGHT -> "⛰";
            case FORCE_COORDS -> "⚑";
            case FORCE_STRUCTURE -> "⌂";
            case FORCE_BIOME -> "☀";
            case FORCE_DEATH -> "☠";
        };
        return "[" + icon + "] " + StringUtility.formatName(objective.getObjectiveString());
    }

    private String truncate(String value) {
        return value.length() > MAX_STRING_LENGTH ? value.substring(0, 21) + "..." : value;
    }

    @Override
    public int getPriority() {
        return 0; // Default priority for ForceBattle
    }
}

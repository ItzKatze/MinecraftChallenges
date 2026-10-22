package gg.itzkatze.mcc.forcebattle;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.configuration.GlobalSetting;
import gg.itzkatze.mcc.configuration.SettingsManager;
import gg.itzkatze.mcc.data.MccAdvancement;
import gg.itzkatze.mcc.team.MccTeam;
import gg.itzkatze.mcc.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

final class ForceBattleNametag {

    private ForceBattleNametag() {
    }

    static @NotNull Component suffix(@NotNull ForceBattlePlayer player) {
        Component suffix = Component.text(" ");

        if (player.isSpectator()) {
            return suffix.append(Component.text("Spectator", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        if (player.isExcluded()) {
            suffix = suffix.append(Component.text("Excluded", NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC));
        } else if (!MinecraftChallenges.getTimer().isRunning()) {
            suffix = suffix.append(Component.text("Waiting", NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC));
        }

        ForceBattleObjective objective = player.getObjective();
        if (objective == null && player.isInTeam()) {
            objective = (ForceBattleObjective) player.getTeam().getObjective();
        }

        if (objective != null) {
            boolean hideObjectives = SettingsManager.isEnabled(GlobalSetting.HIDE_OBJECTIVES);
            boolean hidePoints = SettingsManager.isEnabled(GlobalSetting.HIDE_POINTS);

            if (!hideObjectives) {
                suffix = suffix.append(Component.text(" ")).append(objectiveText(objective));
            }

            if (!hideObjectives && !hidePoints) {
                suffix = suffix.append(Component.text(" |", NamedTextColor.DARK_GRAY));
            }

            if (!hidePoints) {
                suffix = suffix.append(Component.text(" "))
                        .append(Component.text("Points", NamedTextColor.GRAY))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(player.getPoints(), NamedTextColor.BLUE));
            }
        }

        MccTeam teamData = player.getTeam();
        if (teamData != null) {
            suffix = suffix.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Team", NamedTextColor.GRAY))
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(teamData.getId(), NamedTextColor.BLUE));
        }

        return suffix.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    static @NotNull Component objectiveText(@NotNull ForceBattleObjective objective) {
        MccAdvancement advancement = objective.getObjectiveValue(MccAdvancement.class);
        String objectiveString = advancement != null ? advancement.name : StringUtility.formatName(objective.getObjectiveString());
        return Component.text(objective.getBattleType().getPrefix(), NamedTextColor.GRAY)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(objectiveString, NamedTextColor.BLUE));
    }
}

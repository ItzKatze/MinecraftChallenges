package gg.itzkatze.katze.forcebattle.bossbar;

import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.bossbar.BossbarKind;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForceBattleTeamObjectiveBossbar implements BossbarContent {

    @Override
    @NotNull public BossbarKind getKind() {
        return BossbarKind.RUNNING;
    }

    @Override
    @Nullable public BossBar getBossbar(@NotNull KatzePlayer player) {
        if (!player.isInTeam()) return null;

        KatzeTeam team = player.getTeam();
        ForceBattleObjective objective = (ForceBattleObjective) team.getObjective();
        if (objective == null) return null;

        return BossBar.bossBar(
                getTitle(team),
                1,
                BossBar.Color.PURPLE,
                BossBar.Overlay.PROGRESS
        );
    }

    private @NotNull Component getTitle(KatzeTeam team) {
        ForceBattleObjective teamObjective = (ForceBattleObjective) team.getObjective();
        if (teamObjective == null) return Component.empty();

        if (SettingsManager.isEnabled(GlobalSetting.HIDE_POINTS)) {
            return Component.text(teamObjective.getBattleType().getPrefix(), NamedTextColor.GRAY)
                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(StringUtility.formatName(teamObjective.getObjectiveString()), NamedTextColor.BLUE));
        }

        return Component.text(teamObjective.getBattleType().getPrefix(), NamedTextColor.GRAY)
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(Component.text(StringUtility.formatName(teamObjective.getObjectiveString()), NamedTextColor.BLUE))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Team-Points", NamedTextColor.GRAY))
                .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                .append(Component.text(team.getPoints(), NamedTextColor.BLUE));
    }
}

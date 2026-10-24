package gg.itzkatze.katze.forcebattle.bossbar;

import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.bossbar.BossbarKind;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForceBattlePlayerObjectiveBossbar implements BossbarContent {

    @Override
    @NotNull public BossbarKind getKind() {
        return BossbarKind.RUNNING;
    }

    @Override
    @Nullable public BossBar getBossbar(@NotNull KatzePlayer katzePlayer) {
        ForceBattlePlayer player = (ForceBattlePlayer) katzePlayer;
        ForceBattleObjective objective = player.getObjective();
        if (objective == null) return null;

        return BossBar.bossBar(
                getTitle(player),
                1,
                BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS
        );
    }

    private @NotNull Component getTitle(KatzePlayer katzePlayer) {
        ForceBattlePlayer player = (ForceBattlePlayer) katzePlayer;
        ForceBattleObjective playerObjective = player.getObjective();

        if (playerObjective == null) return Component.empty();

        boolean isPlayerInTeam = katzePlayer.isInTeam();
        int points = isPlayerInTeam ? katzePlayer.getTeam().getPoints() : katzePlayer.getPoints();

        if (SettingsManager.isEnabled(GlobalSetting.HIDE_POINTS)) {
            return Component.text(playerObjective.getBattleType().getPrefix(), KatzeColor.LIGHT_GRAY)
                    .append(Component.text(": ", KatzeColor.GRAY))
                    .append(Component.text(StringUtility.formatName(playerObjective.getObjectiveString()), KatzeColor.BLUE));
        }

        return Component.text(playerObjective.getBattleType().getPrefix(), KatzeColor.LIGHT_GRAY)
                .append(Component.text(": ", KatzeColor.GRAY))
                .append(Component.text(StringUtility.formatName(playerObjective.getObjectiveString()), KatzeColor.BLUE))
                .append(Component.text(" | ", KatzeColor.GRAY))
                .append(Component.text(isPlayerInTeam ? "Team-Points" : "Points", KatzeColor.LIGHT_GRAY))
                .append(Component.text(": ", KatzeColor.GRAY))
                .append(Component.text(points, KatzeColor.BLUE));
    }
}

package gg.itzkatze.katze.forcebattle.tasks;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.forcebattle.ForceBattleSettings;
import net.kyori.adventure.text.format.NamedTextColor;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.forcebattle.ForceBattleType;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.Toast;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

public class AdvancementTask implements ForceTask {

    @Override
    public void runTick() {
        if (ForceBattleType.FORCE_ADVANCEMENT.getSettingState() == PlayerTeamBothState.OFF) return;
        if (!KatzeGames.getTimer().isRunning()) return;

        for (KatzePlayer player : KatzePlayer.getOnlinePlayers()) {
            ForceBattlePlayer katzePlayer = (ForceBattlePlayer) player;
            if (!katzePlayer.isParticipating()) continue;

            checkPlayerObjective(katzePlayer);
            checkTeamObjective(katzePlayer);
        }
    }

    private void checkPlayerObjective(ForceBattlePlayer player) {
        ForceBattleObjective objective = player.getObjective();
        if (objective == null) return;
        if (objective.getBattleType() != ForceBattleType.FORCE_ADVANCEMENT) return;

        KatzeAdvancement advancement = objective.getObjectiveValue(KatzeAdvancement.class);
        if (advancement != null && hasCompletedAdvancement(player, advancement)) {
            completeObjective(player, objective.getObjectiveString());
            if (SettingsManager.isEnabled(ForceBattleSettings.FORCE_ADVANCEMENT)
                    && KatzeGames.get().getConfig().getBoolean("reset-advancements-on-finish", true)) {
                resetAdvancements(player);
            }
            player.updateObjective(true, false);
        }
    }

    private void checkTeamObjective(ForceBattlePlayer player) {
        if (!player.isInTeam()) return;
        ForceBattleObjective teamObjective = (ForceBattleObjective) player.getTeam().getObjective();
        if (teamObjective == null) return;
        if (teamObjective.getBattleType() != ForceBattleType.FORCE_ADVANCEMENT) return;

        KatzeAdvancement advancement = teamObjective.getObjectiveValue(KatzeAdvancement.class);
        if (advancement != null && hasCompletedAdvancement(player, advancement)) {
            completeObjective(player, teamObjective.getObjectiveString());
            if (SettingsManager.isEnabled(ForceBattleSettings.FORCE_ADVANCEMENT)
                    && KatzeGames.get().getConfig().getBoolean("reset-advancements-on-finish", true)) {
                resetAdvancements(player);
            }
            player.getTeam().updateObjective(player, true, false);
        }
    }

    private boolean hasCompletedAdvancement(ForceBattlePlayer fbPlayer, KatzeAdvancement advancement) {
        Optional<Advancement> advancementOpt = getAdvancement(advancement.getKey());
        Player player = fbPlayer.getPlayer();

        if (player == null) return false;
        if (advancementOpt.isEmpty()) return false;

        AdvancementProgress progress = player.getAdvancementProgress(advancementOpt.get());
        return progress.isDone();
    }

    private void completeObjective(KatzePlayer player, String objective) {
        String formattedObjective = StringUtility.formatName(objective);
        Material toastIcon = Material.BREWING_STAND;
        KatzeGames plugin = KatzeGames.get();

        if (player.isInTeam() && SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE)) {
            player.getTeam().getPlayers().stream()
                    .filter(teammate -> teammate != player)
                    .filter(teammate -> !teammate.isOffline())
                    .forEach(teammate -> {
                        teammate.sendMessage(Component.text(player.getName() + " has finished " + formattedObjective + "."));
                        Toast.display(teammate.getPlayer(), toastIcon, Component.text(formattedObjective).color(NamedTextColor.BLUE), Toast.Style.GOAL, plugin);
                    });
        }

        player.sendMessage(Component.text("You finished " + formattedObjective + "."));
        Toast.display(player.getPlayer(), toastIcon, Component.text(formattedObjective).color(NamedTextColor.BLUE), Toast.Style.GOAL, plugin);
    }

    private @NotNull Optional<Advancement> getAdvancement(NamespacedKey key) {
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement a = it.next();
            if (a.getKey().equals(key)) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    private void resetAdvancements(ForceBattlePlayer fbPlayer) {
        Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
        Player player = fbPlayer.getPlayer();
        if (player == null) return;

        while (iterator.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
    }
}

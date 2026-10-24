package gg.itzkatze.katze.forcebattle.tasks;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.forcebattle.ForceBattleType;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.Toast;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class BiomeTask implements ForceTask {

    @Override
    public void runTick() {
        if (ForceBattleType.FORCE_BIOME.getSettingState() == PlayerTeamBothState.OFF) return;
        if (!KatzeGames.getTimer().isRunning()) return;

        for (KatzePlayer katzePlayer : KatzePlayer.getOnlinePlayers()) {
            ForceBattlePlayer fbPlayer = (ForceBattlePlayer) katzePlayer;
            Player player = katzePlayer.getPlayer();

            if (player == null) continue;
            if (!katzePlayer.isParticipating()) continue;

            Biome currentBiome = player.getWorld().getBiome(player.getLocation());
            checkPlayerObjective(fbPlayer, currentBiome);
            checkTeamObjective(fbPlayer, currentBiome);
        }
    }

    private void checkPlayerObjective(ForceBattlePlayer player, Biome currentBiome) {
        ForceBattleObjective objective = player.getObjective();
        if (objective == null) return;
        if (objective.getBattleType() != ForceBattleType.FORCE_BIOME) return;

        if (isBiomeMatch(objective.getObjectiveValue(), currentBiome)) {
            completeObjective(player, objective.getObjectiveString());
            player.updateObjective(true, false);
        }
    }

    private void checkTeamObjective(ForceBattlePlayer player, Biome currentBiome) {
        if (!player.isInTeam()) return;
        ForceBattleObjective teamObjective = (ForceBattleObjective) player.getTeam().getObjective();
        if (teamObjective == null) return;
        if (teamObjective.getBattleType() != ForceBattleType.FORCE_BIOME) return;

        if (isBiomeMatch(teamObjective.getObjectiveValue(), currentBiome)) {
            completeObjective(player, teamObjective.getObjectiveString());
            player.getTeam().updateObjective(player, true, false);
        }
    }

    private boolean isBiomeMatch(Object objectiveValue, Biome currentBiome) {
        if (objectiveValue instanceof BiomeSimplified simplified) {
            return simplified.getBiomes().contains(currentBiome);
        }

        if (objectiveValue instanceof Biome biome) {
            return biome == currentBiome;
        }

        return false;
    }

    private void completeObjective(ForceBattlePlayer player, String objective) {
        String formattedObjective = StringUtility.formatName(objective);
        Material toastIcon = Material.GRASS_BLOCK;
        KatzeGames plugin = KatzeGames.get();

        if (player.isInTeam() && SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE)) {
            player.getTeam().getPlayers().stream()
                    .filter(teammate -> teammate != player)
                    .filter(teammate -> !teammate.isOffline())
                    .forEach(teammate -> {
                        teammate.sendMessage(Component.text(player.getName() + " has finished " + formattedObjective + "."));
                        Toast.display(teammate.getPlayer(), toastIcon, Component.text(formattedObjective).color(KatzeColor.BLUE), Toast.Style.GOAL, plugin);
                    });
        }

        player.sendMessage(Component.text("You finished " + formattedObjective + "."));
        Toast.display(player.getPlayer(), toastIcon, Component.text(formattedObjective).color(KatzeColor.BLUE), Toast.Style.GOAL, plugin);
    }
}

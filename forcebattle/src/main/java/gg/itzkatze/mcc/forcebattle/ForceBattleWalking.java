package gg.itzkatze.mcc.forcebattle;

import gg.itzkatze.mcc.level.GameXp;
import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.player.MccPlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class ForceBattleWalking {

    static final int CM_PER_XP = 10_000;
    private static final List<Statistic> ON_FOOT = List.of(Statistic.WALK_ONE_CM, Statistic.SPRINT_ONE_CM, Statistic.CROUCH_ONE_CM,
            Statistic.WALK_ON_WATER_ONE_CM, Statistic.WALK_UNDER_WATER_ONE_CM);

    private final Map<UUID, Integer> baselines = new HashMap<>();
    private final Map<UUID, Integer> remainders = new HashMap<>();

    void tick() {
        if (!GameXp.isGameRunning()) {
            baselines.clear();
            return;
        }

        Set<UUID> counted = new HashSet<>();
        for (MccPlayer participant : MccPlayer.getOnlinePlayers()) {
            Player player = participant.getPlayer();
            if (!(participant instanceof ForceBattlePlayer) || player == null || !participant.isParticipating()) continue;

            UUID id = participant.getUuid();
            counted.add(id);
            int total = onFoot(player);
            Integer baseline = baselines.put(id, total);
            if (baseline == null) continue;

            int walked = remainders.getOrDefault(id, 0) + Math.max(0, total - baseline);
            remainders.put(id, walked % CM_PER_XP);
            if (walked >= CM_PER_XP) {
                LevelStore.get().grant(participant, walked / CM_PER_XP * ForceBattleXp.WALKING.xp());
            }
        }
        baselines.keySet().retainAll(counted);
    }

    void reset() {
        baselines.clear();
        remainders.clear();
    }

    private static int onFoot(@NotNull Player player) {
        long centimeters = 0;
        for (Statistic statistic : ON_FOOT) {
            centimeters += player.getStatistic(statistic);
        }
        return (int) Math.min(Integer.MAX_VALUE, centimeters);
    }
}

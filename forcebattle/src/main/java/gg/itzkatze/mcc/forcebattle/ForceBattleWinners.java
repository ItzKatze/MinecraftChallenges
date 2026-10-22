package gg.itzkatze.mcc.forcebattle;

import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.team.MccTeam;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class ForceBattleWinners {

    private record Competitor(int points, @NotNull List<ForceBattlePlayer> members) {
    }

    private ForceBattleWinners() {
    }

    static @NotNull List<ForceBattlePlayer> winners() {
        List<Competitor> competitors = new ArrayList<>();
        for (MccTeam team : MccTeam.teams) {
            if (!(team instanceof ForceBattleTeam forceBattleTeam)) continue;

            List<ForceBattlePlayer> members = forceBattleTeam.getPlayers().stream()
                    .filter(ForceBattlePlayer.class::isInstance)
                    .map(ForceBattlePlayer.class::cast)
                    .filter(MccPlayer::isParticipating)
                    .toList();
            if (!members.isEmpty()) {
                competitors.add(new Competitor(forceBattleTeam.getPoints(), members));
            }
        }
        for (MccPlayer player : MccPlayer.PLAYERS) {
            if (player instanceof ForceBattlePlayer forceBattlePlayer && forceBattlePlayer.isParticipating() && !forceBattlePlayer.isInTeam()) {
                competitors.add(new Competitor(forceBattlePlayer.getPoints(), List.of(forceBattlePlayer)));
            }
        }
        if (competitors.size() < 2) return List.of();

        int top = competitors.stream().mapToInt(Competitor::points).max().orElse(0);
        if (top <= 0) return List.of();

        return competitors.stream()
                .filter(competitor -> competitor.points() == top)
                .flatMap(competitor -> competitor.members().stream())
                .toList();
    }

    static void reward() {
        winners().forEach(winner -> LevelStore.get().grant(winner, ForceBattleXp.WIN.xp()));
    }
}

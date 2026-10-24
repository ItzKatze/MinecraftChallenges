package gg.itzkatze.katze.challenges;

import org.jetbrains.annotations.NotNull;

public interface ChallengeObjective {

    /**
     * Display string for this objective (e.g. for scoreboard/bossbar).
     */
    @NotNull String getObjectiveString();
}

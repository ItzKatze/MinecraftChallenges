package gg.itzkatze.katze.lobby.cosmetics;

import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public enum LeaderboardMetric {

    LIFETIME_POINTS("Lifetime Points", CosmeticProfile::earned),
    BEST_RUN("Best Run", CosmeticProfile::best),
    CURRENT_POINTS("Current Points", CosmeticProfile::points),
    CASES_OPENED("Cases Opened", CosmeticProfile::casesOpened);

    private final String title;
    private final ToIntFunction<CosmeticProfile> reader;

    LeaderboardMetric(String title, ToIntFunction<CosmeticProfile> reader) {
        this.title = title;
        this.reader = reader;
    }

    public @NotNull String title() {
        return title;
    }

    public @NotNull LeaderboardMetric next() {
        LeaderboardMetric[] metrics = values();
        return metrics[(ordinal() + 1) % metrics.length];
    }

    int read(@NotNull CosmeticProfile profile) {
        return reader.applyAsInt(profile);
    }
}

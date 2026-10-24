package gg.itzkatze.katze.spectator;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum SpectatorReason {
    CHOSEN("chosen", false),
    LATE_JOIN("late_join", false),
    ELIMINATED("eliminated", true);

    private final String id;
    private final boolean ranked;

    SpectatorReason(String id, boolean ranked) {
        this.id = id;
        this.ranked = ranked;
    }

    public static @Nullable SpectatorReason fromId(@Nullable String id) {
        for (SpectatorReason reason : values()) {
            if (reason.id.equals(id)) {
                return reason;
            }
        }
        return null;
    }
}

package gg.itzkatze.katze.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@FunctionalInterface
public interface PlayerAdapterFactory {

    /**
     * Creates a challenge-specific player wrapper for the given player, who may be offline.
     * @param uuid The player's UUID
     * @param name The player's name
     * @return The wrapped player
     */
    @NotNull KatzePlayer adapt(@NotNull UUID uuid, @NotNull String name);
}

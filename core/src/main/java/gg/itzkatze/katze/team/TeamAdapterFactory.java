package gg.itzkatze.katze.team;

import gg.itzkatze.katze.player.KatzePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Factory for challenge-specific team wrappers.
 * Challenge modules register their factory in {@link KatzeTeam#registerFactory} when loaded.
 */
@FunctionalInterface
public interface TeamAdapterFactory {

    /**
     * Creates a challenge-specific team wrapper for the given players.
     * @param players The team members
     * @param id The team id to keep consistent across upgrades
     * @param register Whether to register the team in {@link KatzeTeam#teams}
     * @return The wrapped team
     */
    @NotNull KatzeTeam adapt(@NotNull List<KatzePlayer> players, int id, boolean register);
}

package gg.itzkatze.katze.spectator;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** A read-only screen about one player, refreshed while it is open and closed when that player leaves. */
public interface SpectatorView {

    @NotNull UUID targetId();

    @NotNull String targetName();
}

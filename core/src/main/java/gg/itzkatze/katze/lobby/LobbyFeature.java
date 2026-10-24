package gg.itzkatze.katze.lobby;

import gg.itzkatze.katze.player.KatzePlayer;
import org.jetbrains.annotations.NotNull;

public interface LobbyFeature {

    default void onCreate(@NotNull Lobby lobby) {
    }

    default void onDestroy(@NotNull Lobby lobby) {
    }

    default void onPlayerJoin(@NotNull KatzePlayer player, @NotNull Lobby lobby) {
    }
}

package gg.itzkatze.katze.scoreboard;

import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ScoreboardContent {

    @NotNull Component getTitle();

    @NotNull Component[] getLines(@NotNull KatzePlayer player);

    // Higher priority content appears first.
    default int getPriority() {
        return 0;
    }
}

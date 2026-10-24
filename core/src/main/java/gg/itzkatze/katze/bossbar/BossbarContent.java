package gg.itzkatze.katze.bossbar;

import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BossbarContent {

    @NotNull BossbarKind getKind();

    @Nullable BossBar getBossbar(@NotNull KatzePlayer katzePlayer);

    default int getPriority() {
        return 0;
    }
}

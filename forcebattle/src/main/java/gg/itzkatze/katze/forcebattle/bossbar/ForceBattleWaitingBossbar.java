package gg.itzkatze.katze.forcebattle.bossbar;

import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.bossbar.BossbarKind;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForceBattleWaitingBossbar implements BossbarContent {

    @Override
    @NotNull public BossbarKind getKind() {
        return BossbarKind.NOT_RUNNING;
    }

    @Override
    @Nullable public BossBar getBossbar(@NotNull KatzePlayer player) {
        return BossBar.bossBar(
                Component.text("Waiting").color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                1,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );
    }
}

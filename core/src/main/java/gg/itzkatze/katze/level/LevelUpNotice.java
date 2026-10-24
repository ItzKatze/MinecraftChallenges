package gg.itzkatze.katze.level;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public final class LevelUpNotice {

    private static final Sound LEVEL_UP = Sound.sound(Key.key("entity.player.levelup"), Sound.Source.MASTER, 1f, 1f);
    private static final Title.Times TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500));

    private LevelUpNotice() {
    }

    public static void install(@NotNull LevelStore store) {
        store.addLevelUpListener(LevelUpNotice::show);
    }

    private static void show(@NotNull UUID id, int oldLevel, int newLevel) {
        Player player = Bukkit.getPlayer(id);
        if (player == null) return;

        Component subtitle = Component.textOfChildren(
                Component.text(oldLevel, LevelTag.colorOf(oldLevel)),
                Component.text(" → ", KatzeColor.LIGHT_GRAY),
                Component.text(newLevel, LevelTag.colorOf(newLevel)));
        player.showTitle(Title.title(Component.text("Level Up!", KatzeColor.GOLD), subtitle, TIMES));
        player.playSound(LEVEL_UP);
        player.sendMessage(Component.textOfChildren(
                Component.text("You reached level ", KatzeColor.GOLD),
                Component.text(newLevel, LevelTag.colorOf(newLevel)),
                Component.text("!", KatzeColor.GOLD)));
    }
}

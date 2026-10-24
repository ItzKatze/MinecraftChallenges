package gg.itzkatze.katze.level;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public final class LevelTag {

    private LevelTag() {
    }

    public static @NotNull TextColor colorOf(int level) {
        if (level >= 100) return KatzeColor.DARK_RED;
        if (level >= 75) return KatzeColor.RED;
        if (level >= 50) return KatzeColor.GOLD;
        if (level >= 40) return KatzeColor.MAGENTA;
        if (level >= 30) return KatzeColor.BLUE;
        if (level >= 20) return KatzeColor.LIGHT_BLUE;
        if (level >= 10) return KatzeColor.LIME;
        return KatzeColor.LIGHT_GRAY;
    }

    public static @NotNull Component of(int level) {
        return Component.text("[" + level + "]", colorOf(level));
    }
}

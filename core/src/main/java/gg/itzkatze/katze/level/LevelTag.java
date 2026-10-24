package gg.itzkatze.katze.level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public final class LevelTag {

    private LevelTag() {
    }

    public static @NotNull TextColor colorOf(int level) {
        if (level >= 100) return NamedTextColor.DARK_RED;
        if (level >= 75) return NamedTextColor.RED;
        if (level >= 50) return NamedTextColor.GOLD;
        if (level >= 40) return NamedTextColor.LIGHT_PURPLE;
        if (level >= 30) return NamedTextColor.BLUE;
        if (level >= 20) return NamedTextColor.AQUA;
        if (level >= 10) return NamedTextColor.GREEN;
        return NamedTextColor.GRAY;
    }

    public static @NotNull Component of(int level) {
        return Component.text("[" + level + "]", colorOf(level));
    }
}

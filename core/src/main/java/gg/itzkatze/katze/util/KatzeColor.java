package gg.itzkatze.katze.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public final class KatzeColor {

    // One per dye, named after it: the matching NamedTextColor where there is one, else the dye's own color.
    public static final TextColor WHITE = NamedTextColor.WHITE;
    public static final TextColor ORANGE = dye(DyeColor.ORANGE);
    public static final TextColor MAGENTA = NamedTextColor.LIGHT_PURPLE;
    public static final TextColor LIGHT_BLUE = NamedTextColor.AQUA;
    public static final TextColor YELLOW = NamedTextColor.YELLOW;
    public static final TextColor LIME = NamedTextColor.GREEN;
    public static final TextColor PINK = dye(DyeColor.PINK);
    public static final TextColor GRAY = NamedTextColor.DARK_GRAY;
    public static final TextColor LIGHT_GRAY = NamedTextColor.GRAY;
    public static final TextColor CYAN = NamedTextColor.DARK_AQUA;
    public static final TextColor PURPLE = NamedTextColor.DARK_PURPLE;
    public static final TextColor BLUE = NamedTextColor.BLUE;
    public static final TextColor BROWN = dye(DyeColor.BROWN);
    public static final TextColor GREEN = NamedTextColor.DARK_GREEN;
    public static final TextColor RED = NamedTextColor.RED;
    public static final TextColor BLACK = NamedTextColor.BLACK;

    // The named colors no dye matches.
    public static final TextColor GOLD = NamedTextColor.GOLD;
    public static final TextColor DARK_RED = NamedTextColor.DARK_RED;
    public static final TextColor DARK_BLUE = NamedTextColor.DARK_BLUE;

    private KatzeColor() {
    }

    private static @NotNull TextColor dye(@NotNull DyeColor dye) {
        return TextColor.color(dye.getColor().asRGB());
    }
}

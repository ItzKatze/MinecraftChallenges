package gg.itzkatze.katze.lobby.jumpandrun;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;

enum RunColor {

    RED(DyeColor.RED),
    BLUE(DyeColor.BLUE),
    LIME(DyeColor.LIME),
    YELLOW(DyeColor.YELLOW),
    MAGENTA(DyeColor.MAGENTA),
    CYAN(DyeColor.CYAN),
    ORANGE(DyeColor.ORANGE),
    WHITE(DyeColor.WHITE),
    PINK(DyeColor.PINK),
    PURPLE(DyeColor.PURPLE),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    GREEN(DyeColor.GREEN),
    BROWN(DyeColor.BROWN),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    GRAY(DyeColor.GRAY),
    BLACK(DyeColor.BLACK);

    private final Material block;
    private final String displayName;
    private final TextColor textColor;
    private final RunLook look;

    RunColor(DyeColor dye) {
        this.block = Material.valueOf(dye.name() + "_CONCRETE");
        this.displayName = dye.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        this.textColor = TextColor.color(dye.getColor().asRGB());
        this.look = RunLook.of(this);
    }

    @NotNull RunLook look() {
        return look;
    }

    @NotNull Material block() {
        return block;
    }

    @NotNull String displayName() {
        return displayName;
    }

    @NotNull TextColor textColor() {
        return textColor;
    }

    static @NotNull RunColor pick(@NotNull Collection<RunColor> inUse, @Nullable RunColor preferred) {
        if (preferred != null && !inUse.contains(preferred)) return preferred;

        for (RunColor color : values()) {
            if (!inUse.contains(color)) return color;
        }
        return values()[inUse.size() % values().length];
    }
}

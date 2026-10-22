package gg.itzkatze.mcc.lobby.cosmetics;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public enum CosmeticCategory {

    BLOCK("Run Blocks", Material.GRASS_BLOCK),
    SOUND("Landing Sounds", Material.NOTE_BLOCK),
    MUSIC("Music", Material.JUKEBOX);

    private final String title;
    private final Material icon;

    CosmeticCategory(String title, Material icon) {
        this.title = title;
        this.icon = icon;
    }

    public @NotNull String title() {
        return title;
    }

    public @NotNull Material icon() {
        return icon;
    }
}

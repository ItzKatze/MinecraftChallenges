package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public enum Rarity {

    COMMON(NamedTextColor.WHITE, "Common"),
    UNCOMMON(NamedTextColor.GREEN, "Uncommon"),
    RARE(NamedTextColor.BLUE, "Rare"),
    EPIC(NamedTextColor.DARK_PURPLE, "Epic"),
    LEGENDARY(NamedTextColor.GOLD, "Legendary"),
    MYTHIC(NamedTextColor.LIGHT_PURPLE, "Mythic"),
    ;

    private final NamedTextColor color;
    private final String displayName;

    Rarity(NamedTextColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public @NotNull NamedTextColor color() {
        return color;
    }

    public @NotNull String displayName() {
        return displayName;
    }
}

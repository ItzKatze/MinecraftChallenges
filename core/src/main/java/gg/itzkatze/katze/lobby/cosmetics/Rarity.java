package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public enum Rarity {

    COMMON(KatzeColor.WHITE, "Common"),
    UNCOMMON(KatzeColor.LIME, "Uncommon"),
    RARE(KatzeColor.BLUE, "Rare"),
    EPIC(KatzeColor.PURPLE, "Epic"),
    LEGENDARY(KatzeColor.GOLD, "Legendary"),
    MYTHIC(KatzeColor.MAGENTA, "Mythic"),
    ;

    private final TextColor color;
    private final String displayName;

    Rarity(TextColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public @NotNull TextColor color() {
        return color;
    }

    public @NotNull String displayName() {
        return displayName;
    }
}

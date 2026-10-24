package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public interface Cosmetic {

    @NotNull String id();

    @NotNull CosmeticCategory category();

    @NotNull String displayName();

    @NotNull Rarity rarity();

    @NotNull Material icon();

    default @Nullable Season season() {
        return null;
    }

    default boolean isAvailable(@NotNull LocalDate today) {
        Season season = season();
        return season == null || season.isActive(today);
    }

    default @NotNull Component styledName() {
        return Component.text(displayName(), rarity().color());
    }
}

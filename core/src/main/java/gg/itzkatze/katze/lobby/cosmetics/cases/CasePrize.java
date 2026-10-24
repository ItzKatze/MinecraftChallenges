package gg.itzkatze.katze.lobby.cosmetics.cases;

import gg.itzkatze.katze.lobby.cosmetics.Cosmetic;
import gg.itzkatze.katze.lobby.cosmetics.Rarity;
import org.jetbrains.annotations.NotNull;

public sealed interface CasePrize {

    @NotNull Rarity rarity();

    record Points(int amount, @NotNull Rarity rarity) implements CasePrize {
    }

    record Item(@NotNull Cosmetic cosmetic) implements CasePrize {

        @Override
        public @NotNull Rarity rarity() {
            return cosmetic.rarity();
        }
    }
}

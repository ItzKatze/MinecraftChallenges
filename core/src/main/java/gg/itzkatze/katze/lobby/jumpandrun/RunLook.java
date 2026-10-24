package gg.itzkatze.katze.lobby.jumpandrun;

import gg.itzkatze.katze.lobby.cosmetics.BlockCosmetic;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record RunLook(@NotNull Material block, @Nullable RunColor color, @NotNull Component startMessage) {

    static @NotNull RunLook of(@NotNull RunColor color) {
        Component message = Component.text("Jump & Run: jump onto the ", KatzeColor.YELLOW)
                .append(Component.text(color.displayName(), color.textColor()))
                .append(Component.text(" block!", KatzeColor.YELLOW));
        return new RunLook(color.block(), color, message);
    }

    static @NotNull RunLook of(@NotNull BlockCosmetic cosmetic) {
        Component message = Component.text("Jump & Run: jump onto your ", KatzeColor.YELLOW)
                .append(cosmetic.styledName())
                .append(Component.text("!", KatzeColor.YELLOW));
        return new RunLook(cosmetic.material(), null, message);
    }
}

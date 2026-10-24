package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class CosmeticGuiItems {

    private CosmeticGuiItems() {
    }

    static @NotNull GUIItem points(int slot) {
        return new GUIItem(slot) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return ItemStackCreator.getStack(Component.text("Points: " + CosmeticStore.get().points(viewer.getUuid()), KatzeColor.GOLD), Material.GOLD_NUGGET,
                        List.of(Component.text("Every block you land on in the Jump & Run earns one point.", KatzeColor.LIGHT_GRAY),
                                Component.text("Spend them on the Cosmetic Case.", KatzeColor.LIGHT_GRAY)));
            }
        };
    }

    static void play(@NotNull KatzePlayer player, @NotNull Sound sound) {
        Player bukkit = player.getPlayer();
        if (bukkit != null) {
            bukkit.playSound(sound);
        }
    }
}

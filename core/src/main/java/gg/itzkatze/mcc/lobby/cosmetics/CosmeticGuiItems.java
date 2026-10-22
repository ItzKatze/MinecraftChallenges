package gg.itzkatze.mcc.lobby.cosmetics;

import gg.itzkatze.mcc.gui.GUIItem;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.ItemStackCreator;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            public ItemStack getItem(MccPlayer viewer) {
                return ItemStackCreator.getStack(Component.text("Points: " + CosmeticStore.get().points(viewer.getUuid()), NamedTextColor.GOLD), Material.GOLD_NUGGET,
                        List.of(Component.text("Every block you land on in the Jump & Run earns one point.", NamedTextColor.GRAY),
                                Component.text("Spend them on the Cosmetic Case.", NamedTextColor.GRAY)));
            }
        };
    }

    static void play(@NotNull MccPlayer player, @NotNull Sound sound) {
        Player bukkit = player.getPlayer();
        if (bukkit != null) {
            bukkit.playSound(sound);
        }
    }
}

package gg.itzkatze.katze.util;

import gg.itzkatze.katze.KatzeGames;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Joker {
    private static final NamespacedKey PLAYER_SKIP_KEY = new NamespacedKey(KatzeGames.get(), "playerSkipItem");
    private static final NamespacedKey TEAM_SKIP_KEY = new NamespacedKey(KatzeGames.get(), "teamSkipItem");
    private static final NamespacedKey SWAP_KEY = new NamespacedKey(KatzeGames.get(), "swapItem");

    public static boolean isSwapItem(ItemStack stack) {
        return ItemStackCreator.hasData(stack, SWAP_KEY);
    }

    public static boolean isPlayerSkipItem(ItemStack stack) {
        return ItemStackCreator.hasData(stack, PLAYER_SKIP_KEY);
    }

    public static boolean isTeamSkipItem(ItemStack stack) {
        return ItemStackCreator.hasData(stack, TEAM_SKIP_KEY);
    }

    public static ItemStack getSwapItem(int amount) {
        return ItemStackCreator.createSpecialItem(
                Material.STRUCTURE_VOID,
                Component.text("Swap").color(KatzeColor.BLUE),
                List.of(Component.text("Click to swap your objective with another player.").color(KatzeColor.LIGHT_GRAY)),
                SWAP_KEY,
                amount
        );
    }

    public static ItemStack getPlayerSkipItem(int amount) {
        return ItemStackCreator.createSpecialItem(
                Material.BARRIER,
                Component.text("Skip").color(KatzeColor.RED),
                List.of(Component.text("Click to skip your current objective.").color(KatzeColor.LIGHT_GRAY)),
                PLAYER_SKIP_KEY,
                amount
        );
    }

    public static ItemStack getTeamSkipItem(int amount) {
        return ItemStackCreator.createSpecialItem(
                Material.KNOWLEDGE_BOOK,
                Component.text("Skip").color(KatzeColor.RED),
                List.of(Component.text("Click to skip your team's current objective.").color(KatzeColor.LIGHT_GRAY)),
                TEAM_SKIP_KEY,
                amount
        );
    }
}

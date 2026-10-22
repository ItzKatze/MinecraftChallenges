package gg.itzkatze.mcc.lobby.cosmetics.cases;

import gg.itzkatze.mcc.lobby.cosmetics.Cosmetic;
import gg.itzkatze.mcc.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.mcc.lobby.cosmetics.PointsText;
import gg.itzkatze.mcc.lobby.cosmetics.PurchaseConfirmGui;
import gg.itzkatze.mcc.lobby.cosmetics.Rarity;
import gg.itzkatze.mcc.lobby.cosmetics.ShopSounds;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CaseConfirmGui extends PurchaseConfirmGui {

    public CaseConfirmGui() {
        super("Open a Cosmetic Case?");
    }

    public static void offer(@NotNull MccPlayer player) {
        Player bukkit = player.getPlayer();
        if (bukkit == null) return;

        int balance = CosmeticStore.get().points(bukkit.getUniqueId());
        if (balance < CaseLoot.PRICE) {
            bukkit.sendMessage(Component.text(PointsText.missing(CaseLoot.PRICE - balance), NamedTextColor.RED));
            bukkit.playSound(ShopSounds.DENY);
            return;
        }
        new CaseConfirmGui().open(player);
    }

    static @NotNull Component pityLine(@NotNull UUID player) {
        CosmeticStore store = CosmeticStore.get();
        List<Cosmetic> missing = CaseLoot.missing(CaseLoot.available(store.catalog(), store.today()), cosmetic -> store.owns(player, cosmetic));
        if (missing.isEmpty()) {
            return Component.text("You own every cosmetic in the case right now.", NamedTextColor.GRAY);
        }
        int streak = store.duplicateStreak(player);
        if (CaseLoot.pityDue(streak, missing)) {
            return Component.text("Your next case gives a new cosmetic!", NamedTextColor.LIGHT_PURPLE);
        }
        int left = CaseLoot.PITY_STREAK - streak;
        return Component.text(left + (left == 1 ? " more duplicate" : " more duplicates") + " until a guaranteed new cosmetic", NamedTextColor.GRAY);
    }

    @Override
    protected @NotNull ItemStack summary(@NotNull MccPlayer viewer) {
        int balance = CosmeticStore.get().points(viewer.getUuid());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Price: " + PointsText.amount(CaseLoot.PRICE), NamedTextColor.GOLD));
        lore.add(balance >= CaseLoot.PRICE ? Component.text("After opening: " + PointsText.amount(balance - CaseLoot.PRICE), NamedTextColor.GRAY)
                : Component.text(PointsText.missing(CaseLoot.PRICE - balance), NamedTextColor.RED));
        lore.add(pityLine(viewer.getUuid()));
        lore.add(Component.empty());
        lore.add(Component.text("Contains points and cosmetics:", NamedTextColor.GRAY));
        lore.add(rarities());
        return ItemStackCreator.getStack(Component.text("Cosmetic Case", NamedTextColor.LIGHT_PURPLE), Material.ENDER_CHEST, lore);
    }

    @Override
    protected @NotNull String acceptHint() {
        return "Open the case for " + PointsText.amount(CaseLoot.PRICE);
    }

    @Override
    protected @NotNull String denyHint() {
        return "Close";
    }

    @Override
    protected void accept(@NotNull MccPlayer clicker) {
        CaseRollGui.begin(clicker);
    }

    @Override
    protected void deny(@NotNull MccPlayer clicker) {
        Player player = clicker.getPlayer();
        if (player == null) return;

        player.playSound(ShopSounds.CLICK);
        player.closeInventory();
    }

    private static @NotNull Component rarities() {
        TextComponent.Builder line = Component.text();
        for (Rarity tier : Rarity.values()) {
            if (tier.ordinal() > 0) {
                line.append(Component.text(" · ", NamedTextColor.GRAY));
            }
            line.append(Component.text(tier.displayName(), tier.color()));
        }
        return line.build();
    }
}

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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CaseConfirmGui extends PurchaseConfirmGui {

    private int count;

    private CaseConfirmGui(int count) {
        super("Open a Cosmetic Case?");
        this.count = count;
    }

    public static void offer(@NotNull MccPlayer player) {
        Player bukkit = player.getPlayer();
        if (bukkit == null) return;

        CosmeticStore store = CosmeticStore.get();
        int balance = store.points(bukkit.getUniqueId());
        if (balance < CaseLoot.PRICE) {
            bukkit.sendMessage(Component.text(PointsText.missing(CaseLoot.PRICE - balance), NamedTextColor.RED));
            bukkit.playSound(ShopSounds.DENY);
            return;
        }
        new CaseConfirmGui(Math.clamp(store.caseCount(bukkit.getUniqueId()), 1, CaseRollGui.MAX_CASES)).open(player);
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
        int price = CaseLoot.PRICE * count;
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Price: " + PointsText.amount(price), NamedTextColor.GOLD));
        lore.add(balance >= price ? Component.text("After opening: " + PointsText.amount(balance - price), NamedTextColor.GRAY)
                : Component.text(PointsText.missing(price - balance), NamedTextColor.RED));
        lore.add(pityLine(viewer.getUuid()));
        lore.add(Component.empty());
        lore.add(Component.text("Contains points and cosmetics:", NamedTextColor.GRAY));
        lore.add(rarities());
        lore.add(Component.empty());
        lore.add(Component.text("Left-click: one more case (up to " + CaseRollGui.MAX_CASES + ")", NamedTextColor.GRAY));
        lore.add(Component.text("Right-click: one fewer case", NamedTextColor.GRAY));
        Component name = Component.text(count == 1 ? "Cosmetic Case" : count + " Cosmetic Cases", NamedTextColor.LIGHT_PURPLE);
        return ItemStackCreator.getStack(name, Material.ENDER_CHEST, count, lore);
    }

    @Override
    protected @NotNull String acceptHint() {
        if (count == 1) return "Open the case for " + PointsText.amount(CaseLoot.PRICE);
        return "Open " + count + " cases for " + PointsText.amount(CaseLoot.PRICE * count);
    }

    @Override
    protected @NotNull String denyHint() {
        return "Close";
    }

    @Override
    protected void clickSummary(@NotNull MccPlayer clicker, @NotNull ClickType click) {
        int change = switch (click) {
            case LEFT, SHIFT_LEFT -> 1;
            case RIGHT, SHIFT_RIGHT -> -1;
            default -> 0;
        };
        Player player = clicker.getPlayer();
        if (change == 0 || player == null) return;

        int next = count + change;
        if (next < 1 || next > CaseRollGui.MAX_CASES) {
            player.playSound(ShopSounds.DENY);
            return;
        }
        count = next;
        CosmeticStore.get().rememberCaseCount(player, count);
        player.playSound(ShopSounds.CLICK);
        redraw(clicker);
    }

    @Override
    protected void accept(@NotNull MccPlayer clicker) {
        if (!CaseRollGui.begin(clicker, count)) {
            redraw(clicker);
        }
    }

    @Override
    protected void deny(@NotNull MccPlayer clicker) {
        Player player = clicker.getPlayer();
        if (player == null) return;

        player.playSound(ShopSounds.CLICK);
        player.closeInventory();
    }

    private void redraw(@NotNull MccPlayer viewer) {
        setItems(viewer);
        updateInventory(viewer);
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

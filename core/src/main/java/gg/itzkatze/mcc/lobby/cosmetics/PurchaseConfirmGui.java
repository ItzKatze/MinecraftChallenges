package gg.itzkatze.mcc.lobby.cosmetics;

import gg.itzkatze.mcc.gui.GUIClickableItem;
import gg.itzkatze.mcc.gui.MccGUI;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public abstract class PurchaseConfirmGui extends MccGUI {

    private static final int ACCEPT_SLOT = 3;
    private static final int SUMMARY_SLOT = 4;
    private static final int DENY_SLOT = 5;

    protected PurchaseConfirmGui(@NotNull String title) {
        super(title, 9);
    }

    protected abstract @NotNull ItemStack summary(@NotNull MccPlayer viewer);

    protected abstract @NotNull String acceptHint();

    protected abstract @NotNull String denyHint();

    protected abstract void accept(@NotNull MccPlayer clicker);

    protected abstract void deny(@NotNull MccPlayer clicker);

    protected void clickSummary(@NotNull MccPlayer clicker, @NotNull ClickType click) {
    }

    @Override
    public final void setItems(MccPlayer player) {
        clear();
        fill(ItemStackCreator.fillerItem());
        set(button(ACCEPT_SLOT, Component.text("Accept", NamedTextColor.GREEN, TextDecoration.BOLD), Material.LIME_STAINED_GLASS_PANE, acceptHint(), this::accept));
        set(new GUIClickableItem(SUMMARY_SLOT) {
            @Override
            public void run(InventoryClickEvent event, MccPlayer clicker) {
                clickSummary(clicker, event.getClick());
            }

            @Override
            public ItemStack getItem(MccPlayer viewer) {
                return summary(viewer);
            }
        });
        set(button(DENY_SLOT, Component.text("Deny", NamedTextColor.RED, TextDecoration.BOLD), Material.RED_STAINED_GLASS_PANE, denyHint(), this::deny));
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private static @NotNull GUIClickableItem button(int slot, @NotNull Component name, @NotNull Material icon, @NotNull String hint, @NotNull Consumer<MccPlayer> action) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, MccPlayer clicker) {
                action.accept(clicker);
            }

            @Override
            public ItemStack getItem(MccPlayer viewer) {
                return ItemStackCreator.getStack(name, icon, List.of(Component.text(hint, NamedTextColor.GRAY)));
            }
        };
    }
}

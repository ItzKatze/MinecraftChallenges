package gg.itzkatze.katze.spectator;

import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BackpackViewGui extends KatzeGUI implements SpectatorView {

    static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int BACK_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final UUID targetId;
    private final String targetName;
    private List<ItemStack> contents = List.of();
    private int page;

    public BackpackViewGui(@NotNull KatzePlayer target) {
        super(target.getName() + "'s Backpack", 54);
        this.targetId = target.getUuid();
        this.targetName = target.getName();
    }

    @Override
    public @NotNull UUID targetId() {
        return targetId;
    }

    @Override
    public @NotNull String targetName() {
        return targetName;
    }

    @Override
    public void setItems(KatzePlayer viewer) {
        fill(ItemStackCreator.fillerItem(), PAGE_SIZE, size - 1, true);
        for (int slot = 0; slot < PAGE_SIZE; slot++) {
            set(entry(slot));
        }
        set(arrow(PREVIOUS_SLOT, "Previous page", -1));
        set(arrow(NEXT_SLOT, "Next page", 1));
        set(backButton());
    }

    // Every render reads the backpack once, so the page and its slots agree even when the backpack shrank since the last one.
    @Override
    public void updateInventory(KatzePlayer viewer) {
        List<ItemStack> current = KatzePlayer.adapt(targetId).map(KatzePlayer::getBackpackContents).orElse(null);
        contents = current == null ? List.of() : current;
        page = Math.clamp(page, 0, pages() - 1);
        super.updateInventory(viewer);
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private int pages() {
        return Math.max(1, Math.ceilDiv(contents.size(), PAGE_SIZE));
    }

    private boolean leadsSomewhere(int step) {
        int target = page + step;
        return target >= 0 && target < pages();
    }

    private @NotNull GUIItem entry(int slot) {
        return new GUIItem(slot) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                int index = page * PAGE_SIZE + slot;
                return index < contents.size() ? contents.get(index) : null;
            }
        };
    }

    private @NotNull GUIClickableItem arrow(int slot, @NotNull String name, int step) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer viewer) {
                if (!leadsSomewhere(step)) return;

                page += step;
                updateInventory(viewer);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                if (!leadsSomewhere(step)) return ItemStackCreator.fillerItem();
                return ItemStackCreator.getStack(Component.text(name, NamedTextColor.YELLOW), Material.ARROW,
                        List.of(Component.text("Page " + (page + step + 1) + " / " + pages(), NamedTextColor.GRAY)));
            }
        };
    }

    private @NotNull GUIClickableItem backButton() {
        return new GUIClickableItem(BACK_SLOT) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer viewer) {
                KatzePlayer.adapt(targetId).ifPresent(target -> new PlayerViewGui(target).open(viewer));
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return ItemStackCreator.createNamedItemStack(Material.OAK_DOOR, Component.text("Back", NamedTextColor.YELLOW));
            }
        };
    }
}

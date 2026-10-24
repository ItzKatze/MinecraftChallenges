package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CosmeticMenuGui extends KatzeGUI {

    private static final int POINTS_SLOT = 4;
    private static final int CLOSE_SLOT = 22;

    public CosmeticMenuGui() {
        super("Cosmetics", 27);
    }

    @Override
    public void setItems(KatzePlayer player) {
        clear();
        fill(ItemStackCreator.fillerItem());
        set(CosmeticGuiItems.points(POINTS_SLOT));
        for (CosmeticCategory category : CosmeticCategory.values()) {
            set(categoryButton(category));
        }
        set(GUIClickableItem.getCloseItem(CLOSE_SLOT));
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private static int slotOf(@NotNull CosmeticCategory category) {
        return switch (category) {
            case BLOCK -> 11;
            case SOUND -> 13;
            case MUSIC -> 15;
        };
    }

    private static @NotNull GUIClickableItem categoryButton(@NotNull CosmeticCategory category) {
        return new GUIClickableItem(slotOf(category)) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer clicker) {
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                new CosmeticCategoryGui(category).open(clicker);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                CosmeticStore store = CosmeticStore.get();
                List<Cosmetic> cosmetics = store.catalog().in(category);
                long collected = cosmetics.stream().filter(cosmetic -> store.owns(viewer.getUuid(), cosmetic)).count();
                return ItemStackCreator.getStack(Component.text(category.title(), KatzeColor.YELLOW, TextDecoration.BOLD), category.icon(),
                        List.of(Component.text("Collected: " + collected + "/" + cosmetics.size(), KatzeColor.LIGHT_GRAY), Component.text("Click to open", KatzeColor.LIGHT_GRAY)));
            }
        };
    }
}

package gg.itzkatze.mcc.lobby.cosmetics;

import gg.itzkatze.mcc.gui.GUIClickableItem;
import gg.itzkatze.mcc.gui.MccGUI;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CosmeticMenuGui extends MccGUI {

    private static final int POINTS_SLOT = 4;
    private static final int CLOSE_SLOT = 22;

    public CosmeticMenuGui() {
        super("Cosmetics", 27);
    }

    @Override
    public void setItems(MccPlayer player) {
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
            public void run(InventoryClickEvent event, MccPlayer clicker) {
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                new CosmeticCategoryGui(category).open(clicker);
            }

            @Override
            public ItemStack getItem(MccPlayer viewer) {
                CosmeticStore store = CosmeticStore.get();
                List<Cosmetic> cosmetics = store.catalog().in(category);
                long collected = cosmetics.stream().filter(cosmetic -> store.owns(viewer.getUuid(), cosmetic)).count();
                return ItemStackCreator.getStack(Component.text(category.title(), NamedTextColor.YELLOW, TextDecoration.BOLD), category.icon(),
                        List.of(Component.text("Collected: " + collected + "/" + cosmetics.size(), NamedTextColor.GRAY), Component.text("Click to open", NamedTextColor.GRAY)));
            }
        };
    }
}

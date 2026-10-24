package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class CosmeticCategoryGui extends KatzeGUI {

    private static final int POINTS_SLOT = 4;
    private static final int PREVIOUS_SLOT = 45;
    private static final int BACK_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_SLOT = 53;
    private static final int[] ENTRY_SLOTS = entrySlots();

    // A null cosmetic is the free Default entry of the category.
    private record Entry(@Nullable Cosmetic cosmetic) {
    }

    private final CosmeticCategory category;
    private int page;

    public CosmeticCategoryGui(@NotNull CosmeticCategory category) {
        super(category.title(), 54);
        this.category = category;
    }

    private static int[] entrySlots() {
        int[] slots = new int[28];
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 7; column++) {
                slots[row * 7 + column] = (row + 1) * 9 + column + 1;
            }
        }
        return slots;
    }

    @Override
    public void setItems(KatzePlayer player) {
        clear();
        fill(ItemStackCreator.fillerItem());
        set(CosmeticGuiItems.points(POINTS_SLOT));
        set(backButton());

        List<Entry> entries = entries();
        int pages = Math.max(1, Math.ceilDiv(entries.size(), ENTRY_SLOTS.length));
        page = Math.clamp(page, 0, pages - 1);
        for (int i = 0; i < ENTRY_SLOTS.length; i++) {
            int index = page * ENTRY_SLOTS.length + i;
            if (index >= entries.size()) break;
            set(entry(ENTRY_SLOTS[i], entries.get(index)));
        }
        if (page > 0) {
            set(arrow(PREVIOUS_SLOT, "Previous page", -1, pages));
        }
        if (page < pages - 1) {
            set(arrow(NEXT_SLOT, "Next page", 1, pages));
        }
        set(GUIClickableItem.getCloseItem(CLOSE_SLOT));
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private void redraw(@NotNull KatzePlayer player) {
        clear();
        setItems(player);
        updateInventory(player);
    }

    private @NotNull List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(null));
        CosmeticStore.get().catalog().in(category).stream()
                .sorted(Comparator.comparing(Cosmetic::rarity))
                .forEach(cosmetic -> entries.add(new Entry(cosmetic)));
        return entries;
    }

    private @NotNull GUIClickableItem backButton() {
        return new GUIClickableItem(BACK_SLOT) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer clicker) {
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                new CosmeticMenuGui().open(clicker);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return ItemStackCreator.getStack(Component.text("Back to Categories", NamedTextColor.YELLOW), Material.ARROW, List.of());
            }
        };
    }

    private @NotNull GUIClickableItem arrow(int slot, @NotNull String name, int step, int pages) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer clicker) {
                page += step;
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                redraw(clicker);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return ItemStackCreator.getStack(Component.text(name, NamedTextColor.YELLOW), Material.ARROW, List.of(Component.text("Page " + (page + 1) + " of " + pages, NamedTextColor.GRAY)));
            }
        };
    }

    private @NotNull GUIClickableItem entry(int slot, @NotNull Entry entry) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer clicker) {
                ClickType type = event.getClick();
                if (type == ClickType.LEFT || type == ClickType.SHIFT_LEFT) {
                    choose(entry, clicker);
                } else if (type == ClickType.RIGHT || type == ClickType.SHIFT_RIGHT) {
                    preview(entry, clicker);
                }
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return render(entry, viewer);
            }
        };
    }

    private @NotNull ItemStack render(@NotNull Entry entry, @NotNull KatzePlayer viewer) {
        CosmeticStore store = CosmeticStore.get();
        UUID id = viewer.getUuid();
        Cosmetic cosmetic = entry.cosmetic();
        boolean owned = cosmetic == null || store.owns(id, cosmetic);
        boolean equipped = cosmetic == null ? store.equipped(id, category).isEmpty() : owned && isEquipped(store, id, cosmetic);

        List<Component> lore = new ArrayList<>();
        if (cosmetic != null) {
            lore.add(Component.text("Rarity: " + cosmetic.rarity().displayName(), cosmetic.rarity().color()));
        }
        if (!owned) {
            lore.add(Component.text("Not owned", NamedTextColor.RED));
        } else if (equipped) {
            lore.add(Component.text("Equipped", NamedTextColor.GREEN));
        } else {
            lore.add(Component.text("Click to equip", NamedTextColor.YELLOW));
        }
        if (cosmetic instanceof MusicCosmetic music && music.isSong() && !MusicChannel.isListening(viewer.getPlayer())) {
            lore.add(Component.text("Needs the client mod", NamedTextColor.GRAY));
        }
        Season season = cosmetic == null ? null : cosmetic.season();
        if (season != null) {
            lore.add(season.availability(store.today()));
        }
        if (previewOf(entry) != null) {
            lore.add(Component.text("Right-click to preview", NamedTextColor.GRAY));
        }

        Component name = cosmetic != null
                ? cosmetic.styledName()
                : Component.text(
                        switch (category) {
                            case BLOCK -> "Default: Colored Concrete";
                            case SOUND -> "Default: XP Orb";
                            case MUSIC -> "Default: No Music";
                        },
                        NamedTextColor.WHITE
                );

        Material icon = cosmetic != null
                ? owned ? cosmetic.icon() : Material.GRAY_DYE
                : switch (category) {
                    case BLOCK -> Material.LIME_CONCRETE;
                    case SOUND -> Material.EXPERIENCE_BOTTLE;
                    case MUSIC -> Material.BARRIER;
                };

        ItemStack stack = ItemStackCreator.getStack(name, icon, lore);
        return equipped ? glowing(stack) : stack;
    }

    private void choose(@NotNull Entry entry, @NotNull KatzePlayer clicker) {
        Player player = clicker.getPlayer();
        if (player == null) return;

        CosmeticStore store = CosmeticStore.get();
        UUID id = player.getUniqueId();
        Cosmetic cosmetic = entry.cosmetic();
        if (cosmetic == null) {
            if (store.equipped(id, category).isPresent()) {
                store.unequip(player, category);
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                redraw(clicker);
            }
        } else if (store.owns(id, cosmetic)) {
            if (!isEquipped(store, id, cosmetic)) {
                store.equip(player, cosmetic);
                CosmeticGuiItems.play(clicker, ShopSounds.CLICK);
                redraw(clicker);
            }
        } else {
            Season season = cosmetic.season();
            player.sendMessage(season != null && !cosmetic.isAvailable(store.today())
                    ? season.returnsMessage(cosmetic)
                    : Component.text("Win " + cosmetic.displayName() + " from the Cosmetic Case.", NamedTextColor.RED));
            CosmeticGuiItems.play(clicker, ShopSounds.DENY);
        }
    }

    private void preview(@NotNull Entry entry, @NotNull KatzePlayer clicker) {
        Sound sound = previewOf(entry);
        if (sound != null) {
            CosmeticGuiItems.play(clicker, sound);
        }
    }

    private @Nullable Sound previewOf(@NotNull Entry entry) {
        Cosmetic cosmetic = entry.cosmetic();
        if (cosmetic == null) {
            return category == CosmeticCategory.SOUND ? SoundCosmetic.DEFAULT : null;
        }
        return cosmetic instanceof SoundCosmetic sound ? sound.sound() : null;
    }

    private static boolean isEquipped(@NotNull CosmeticStore store, @NotNull UUID id, @NotNull Cosmetic cosmetic) {
        return store.equipped(id, cosmetic.category()).map(equipped -> equipped.id().equals(cosmetic.id())).orElse(false);
    }

    private static @NotNull ItemStack glowing(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        stack.setItemMeta(meta);
        return stack;
    }
}

package gg.itzkatze.katze.allitems.gui;

import gg.itzkatze.katze.allitems.AllItems;
import gg.itzkatze.katze.allitems.AllItemsObjectiveType;
import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AllItemsLeaderboardGUI extends KatzeGUI {

    private static final int PAGE_SIZE = 45;
    private final int page;

    public AllItemsLeaderboardGUI() {
        this(0);
    }

    public AllItemsLeaderboardGUI(int page) {
        super("All Items Leaderboard", 54);
        this.page = page;
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        AllItemsData data = AllItems.getData();

        List<KatzePlayer> sorted = new ArrayList<>(KatzePlayer.getOnlinePlayers());
        sorted.sort(Comparator.comparingInt(p -> -getTotalProgress(data, p.getUuid())));

        int start = page * PAGE_SIZE;
        int end = Math.min(sorted.size(), start + PAGE_SIZE);
        for (int i = start; i < end; i++) {
            int slot = i - start;
            KatzePlayer target = sorted.get(i);
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    return ItemStackCreator.getPlayerSkull(
                            target.getUuid(),
                            Component.text(target.getName()).color(NamedTextColor.AQUA),
                            1,
                            buildLore(data, target.getUuid())
                    );
                }
            });
        }

        if (page > 0) {
            set(GUIClickableItem.getGoBackItem(47, new AllItemsLeaderboardGUI(page - 1)));
        }
        if (end < sorted.size()) {
            set(GUIClickableItem.getGoForthItem(51, new AllItemsLeaderboardGUI(page + 1)));
        }
        set(GUIClickableItem.getGoBackItem(45, new AllItemsSettingsGUI()));
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private int getTotalProgress(AllItemsData data, UUID uuid) {
        int total = 0;
        for (AllItemsObjectiveType type : AllItemsObjectiveType.values()) {
            total += data.getFoundCount(type, uuid);
        }
        return total;
    }

    private Component[] buildLore(AllItemsData data, UUID uuid) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Total: ").color(NamedTextColor.GRAY)
                .append(Component.text(getTotalProgress(data, uuid)).color(NamedTextColor.WHITE)));
        lore.add(Component.empty());
        for (AllItemsObjectiveType type : AllItemsObjectiveType.values()) {
            int found = data.getFoundCount(type, uuid);
            int total = switch (type) {
                case ITEM -> data.getTotalAvailableItems();
                case BIOME -> data.getTotalAvailableBiomes();
                case MOB -> data.getTotalAvailableMobs();
                case STRUCTURE -> data.getTotalAvailableStructures();
                case ADVANCEMENT -> data.getTotalAvailableAdvancements();
            };
            lore.add(Component.text(type.getDisplayName() + ": ").color(NamedTextColor.GRAY)
                    .append(Component.text(found + "/" + total).color(NamedTextColor.WHITE)));
        }
        return lore.toArray(new Component[0]);
    }
}

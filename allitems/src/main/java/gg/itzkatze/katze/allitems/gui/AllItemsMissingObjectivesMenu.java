package gg.itzkatze.katze.allitems.gui;

import gg.itzkatze.katze.allitems.AllItemsObjectiveType;
import gg.itzkatze.katze.allitems.AllItemsSettings;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AllItemsMissingObjectivesMenu extends KatzeGUI {

    public AllItemsMissingObjectivesMenu() {
        super("Missing Objectives", 27);
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        setTypeButton(10, AllItemsObjectiveType.ITEM, AllItemsSettings.isItemsEnabled());
        setTypeButton(11, AllItemsObjectiveType.BIOME, AllItemsSettings.isBiomesEnabled());
        setTypeButton(12, AllItemsObjectiveType.MOB, AllItemsSettings.isMobsEnabled());
        setTypeButton(13, AllItemsObjectiveType.STRUCTURE, AllItemsSettings.isStructuresEnabled());
        setTypeButton(14, AllItemsObjectiveType.ADVANCEMENT, AllItemsSettings.isAdvancementsEnabled());

        set(GUIClickableItem.getGoBackItem(18, new AllItemsSettingsGUI()));
    }

    private void setTypeButton(int slot, AllItemsObjectiveType type, boolean enabled) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer player) {
                new AllItemsMissingObjectivesListGUI(type, 0).open(player);
            }

            @Override
            public ItemStack getItem(KatzePlayer p) {
                TextColor color = enabled ? KatzeColor.LIGHT_BLUE : KatzeColor.RED;
                return ItemStackCreator.getStack(
                        Component.text(type.getDisplayName()).color(color),
                        type.getIcon(),
                        1,
                        List.of(
                                Component.text("Status: ").color(KatzeColor.LIGHT_GRAY)
                                        .append(Component.text(enabled ? "Enabled" : "Disabled").color(color)),
                                Component.empty(),
                                Component.text("Click to view").color(KatzeColor.YELLOW)
                        )
                );
            }
        });
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}

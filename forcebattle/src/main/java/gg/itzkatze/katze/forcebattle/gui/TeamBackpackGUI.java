package gg.itzkatze.katze.forcebattle.gui;

import gg.itzkatze.katze.forcebattle.ForceBattleTeam;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.player.KatzePlayer;

public class TeamBackpackGUI extends KatzeGUI {
    private final int index;
    private final ForceBattleTeam team;

    public TeamBackpackGUI(int index, int size, ForceBattleTeam team) {
        super("Team Backpack | Page: " + (index + 1), size);
        this.index = index;
        this.team = team;
    }

    @Override
    public void setItems(final KatzePlayer player) {
        int inventoryPages = team.getBACKPACK_INVENTORIES().size();

        if (index < 0 || index >= inventoryPages) return;

        int nextArrowSlot = size - 1;
        int prevArrowSlot = size - 9;

        synchronized (items) {
            items.removeIf(item -> {
                if (item.slot == nextArrowSlot || item.slot == prevArrowSlot) {
                    return item instanceof GUIClickableItem;
                }
                return false;
            });
        }

        GUIItem nextItem = get(nextArrowSlot);
        if (nextItem instanceof GUIClickableItem) {
            getInventory().setItem(nextArrowSlot, null);
        }

        GUIItem prevItem = get(prevArrowSlot);
        if (prevItem instanceof GUIClickableItem) {
            getInventory().setItem(prevArrowSlot, null);
        }

        if (index < inventoryPages - 1) set(GUIClickableItem.getGoForthItem(size - 1, team.getBACKPACK_INVENTORIES().get(index + 1)));
        if (index > 0) set(GUIClickableItem.getGoBackItem(size - 9, team.getBACKPACK_INVENTORIES().get(index - 1)));
    }

    public boolean isNavigationSlot(int slot) {
        int inventoryPages = team.getBACKPACK_INVENTORIES().size();
        return (slot == size - 1 && index < inventoryPages - 1) || (slot == size - 9 && index > 0);
    }

    @Override
    public boolean allowItemMoving() {
        return true;
    }
}

package gg.itzkatze.katze.forcebattle.gui.info;

import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.gui.GUIMaterial;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InfoSimplifiedStructuresGUI extends KatzeGUI {

    public InfoSimplifiedStructuresGUI() {
        super("Simplified Structures Info", 27);
    }

    @Override
    public void setItems(final KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());
        set(GUIClickableItem.getGoBackItem(21, new InfoGUI()));
        set(GUIClickableItem.getCloseItem(22));

        int index = 0;
        for (StructureSimplified simplified : StructureSimplified.values()) {
            set(new GUIItem(index) {
                @Override
                public ItemStack getItem(final KatzePlayer player) {
                    GUIMaterial guiMaterial = simplified.getGuiMaterial();
                    ArrayList<Component> lore = new ArrayList<>();

                    for (KatzeStructure biome : simplified.getStructures()) {
                        lore.add(Component.text(" • " + biome.getName()).color(KatzeColor.WHITE));
                    }

                    if (guiMaterial.hasSkull()) {
                        return ItemStackCreator.getSkull(guiMaterial.skull(), Component.text(simplified.getName()).color(KatzeColor.GOLD), 1, lore);
                    } else {
                        return ItemStackCreator.getStack(Component.text(simplified.getName()).color(KatzeColor.GOLD), guiMaterial.material(), 1, lore);
                    }
                }
            });

            index++;
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

}

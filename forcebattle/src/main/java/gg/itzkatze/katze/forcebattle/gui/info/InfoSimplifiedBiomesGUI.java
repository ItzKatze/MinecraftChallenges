package gg.itzkatze.katze.forcebattle.gui.info;

import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.gui.GUIMaterial;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InfoSimplifiedBiomesGUI extends KatzeGUI {

    public InfoSimplifiedBiomesGUI() {
        super("Simplified Biomes Info", 36);
    }

    @Override
    public void setItems(final KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());
        set(GUIClickableItem.getGoBackItem(30, new InfoGUI()));
        set(GUIClickableItem.getCloseItem(31));

        int index = 0;
        for (BiomeSimplified simplified : BiomeSimplified.values()) {
            set(new GUIItem(index) {
                @Override
                public ItemStack getItem(final KatzePlayer player) {
                    GUIMaterial guiMaterial = simplified.getGuiMaterial();
                    ArrayList<Component> lore = new ArrayList<>();

                    for (Biome biome : simplified.getBiomes()) {
                        lore.add(Component.text(" • " + StringUtility.formatName(biome.key().value())).color(NamedTextColor.WHITE));
                    }

                    if (guiMaterial.hasSkull()) {
                        return ItemStackCreator.getSkull(guiMaterial.skull(), Component.text(simplified.getName()).color(NamedTextColor.GOLD), 1, lore);
                    } else {
                        return ItemStackCreator.getStack(Component.text(simplified.getName()).color(NamedTextColor.GOLD), guiMaterial.material(), 1, lore);
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

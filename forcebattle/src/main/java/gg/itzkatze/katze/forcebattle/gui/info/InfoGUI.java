package gg.itzkatze.katze.forcebattle.gui.info;

import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InfoGUI extends KatzeGUI {

    public InfoGUI() {
        super("Information", 27);
    }

    @Getter
    private enum SimplifiedTasks {
        BIOMES(10, "Simplified Biomes", Material.GRASS_BLOCK, new InfoSimplifiedBiomesGUI()),
        STRUCTURES(12, "Simplified Structures", Material.STRUCTURE_BLOCK, new InfoSimplifiedStructuresGUI()),
        MOBS(14, "Simplified Mobs", Material.DIAMOND_SWORD, new InfoSimplifiedMobsGUI()),
        DEATHS(16, "Simplified Deaths", Material.SKELETON_SKULL, new InfoSimplifiedDeathsGUI()),
        ;

        private final int slot;
        private final String name;
        private final Material material;
        private final KatzeGUI gui;

        SimplifiedTasks(int slot, String name, Material material, KatzeGUI gui) {
            this.slot = slot;
            this.name = name;
            this.material = material;
            this.gui = gui;
        }
    }

    @Override
    public void setItems(final KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        for (SimplifiedTasks task : SimplifiedTasks.values()) {
            set(new GUIClickableItem(task.getSlot()) {
                @Override
                public void run(final InventoryClickEvent event, final KatzePlayer player) {
                    task.getGui().open(player);
                }

                @Override
                public ItemStack getItem(final KatzePlayer player) {
                    return ItemStackCreator.getStack(Component.text(task.getName()).color(NamedTextColor.BLUE), task.getMaterial(), 1);
                }
            });
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

}

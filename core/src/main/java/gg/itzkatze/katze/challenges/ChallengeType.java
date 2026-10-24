package gg.itzkatze.katze.challenges;

import gg.itzkatze.katze.gui.GUIMaterial;
import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum ChallengeType {
    FORCE_BATTLE("Force Battle", new GUIMaterial(Material.DIAMOND_SWORD), "Complete random objectives to score points"),
    ALL_ITEMS("All Items", new GUIMaterial(Material.CHEST), "Collect every item in Minecraft"),
    DEATH_SWAP("Death Swap", new GUIMaterial(Material.ENDER_PEARL), "Players swap locations on a timer until one survives"),
    RANDOMIZER("Randomizer", new GUIMaterial(Material.SNIFFER_EGG), "Randomizes drops and chest loot"),
    ITEM_RAIN("Item Rain", new GUIMaterial(Material.DROPPER), "Gives each player a random item every few seconds"),
    ;

    private final String name;
    private final GUIMaterial material;
    private final String description;

    ChallengeType(String name, GUIMaterial material, String description) {
        this.name = name;
        this.material = material;
        this.description = description;
    }
}

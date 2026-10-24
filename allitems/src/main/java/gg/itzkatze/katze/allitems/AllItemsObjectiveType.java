package gg.itzkatze.katze.allitems;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum AllItemsObjectiveType {
    ITEM("Items", Material.CHEST),
    BIOME("Biomes", Material.GRASS_BLOCK),
    MOB("Mobs", Material.ZOMBIE_SPAWN_EGG),
    STRUCTURE("Structures", Material.STRUCTURE_BLOCK),
    ADVANCEMENT("Advancements", Material.BOOK);

    private final String displayName;
    private final Material icon;

    AllItemsObjectiveType(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

}

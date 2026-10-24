package gg.itzkatze.katze.data;

import gg.itzkatze.katze.gui.GUIMaterial;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public enum StructureSimplified {
    VILLAGE("Village", new GUIMaterial(Material.OAK_PLANKS), List.of(KatzeStructure.VILLAGE_PLAINS, KatzeStructure.VILLAGE_SAVANNA, KatzeStructure.VILLAGE_SNOWY, KatzeStructure.VILLAGE_TAIGA,
            KatzeStructure.VILLAGE_DESERT)),
    PILLAGER_OUTPOST("Pillager Outpost", new GUIMaterial(Material.DARK_OAK_LOG), List.of(KatzeStructure.PILLAGER_OUTPOST)),
    RUINED_PORTAL("Ruined Portal", new GUIMaterial(Material.CRYING_OBSIDIAN), List.of(
            KatzeStructure.RUINED_PORTAL_MOUNTAIN, KatzeStructure.RUINED_PORTAL_DESERT, KatzeStructure.RUINED_PORTAL_JUNGLE,
            KatzeStructure.RUINED_PORTAL_OCEAN, KatzeStructure.RUINED_PORTAL_SWAMP, KatzeStructure.RUINED_PORTAL_NETHER, KatzeStructure.RUINED_PORTAL)),
    OCEAN_RUIN("Ocean Ruin", new GUIMaterial(Material.MOSSY_STONE_BRICKS), List.of(KatzeStructure.OCEAN_RUIN_COLD, KatzeStructure.OCEAN_RUIN_WARM)),
    TRIAL_CHAMBERS("Trial Chambers", new GUIMaterial(Material.WAXED_OXIDIZED_COPPER), List.of(KatzeStructure.TRIAL_CHAMBERS)),
    SHIPWRECK("Shipwreck", new GUIMaterial(Material.CHEST), List.of(KatzeStructure.SHIPWRECK, KatzeStructure.SHIPWRECK_BEACHED)),
    TRAIL_RUINS("Trail Ruins", new GUIMaterial(Material.GRAVEL), List.of(KatzeStructure.TRAIL_RUINS)),
    MANSION("Mansion", new GUIMaterial(Material.DARK_OAK_PLANKS), List.of(KatzeStructure.MANSION)),
    JUNGLE_PYRAMID("Jungle Pyramid", new GUIMaterial(Material.MOSSY_COBBLESTONE), List.of(KatzeStructure.JUNGLE_PYRAMID)),
    MINESHAFT("Mineshaft", new GUIMaterial(Material.RAIL), List.of(KatzeStructure.MINESHAFT, KatzeStructure.MINESHAFT_MESA)),
    SWAMP_HUT("Swamp Hut", new GUIMaterial(Material.CAULDRON), List.of(KatzeStructure.SWAMP_HUT)),
    END_CITY("End City", new GUIMaterial(Material.END_STONE_BRICKS), List.of(KatzeStructure.END_CITY)),
    ANCIENT_CITY("Ancient City", new GUIMaterial(Material.SCULK_SHRIEKER), List.of(KatzeStructure.ANCIENT_CITY)),
    DESERT_PYRAMID("Desert Pyramid", new GUIMaterial(Material.CHISELED_SANDSTONE), List.of(KatzeStructure.DESERT_PYRAMID)),
    MONUMENT("Monument", new GUIMaterial(Material.PRISMARINE), List.of(KatzeStructure.MONUMENT)),
    IGLOO("Igloo", new GUIMaterial(Material.SNOW_BLOCK), List.of(KatzeStructure.IGLOO)),
    BASTION_REMNANT("Bastion Remnant", new GUIMaterial(Material.POLISHED_BLACKSTONE_BRICKS), List.of(KatzeStructure.BASTION_REMNANT)),
    FORTRESS("Fortress", new GUIMaterial(Material.NETHER_BRICKS), List.of(KatzeStructure.FORTRESS)),
    STRONGHOLD("Stronghold", new GUIMaterial(Material.END_PORTAL_FRAME), List.of(KatzeStructure.STRONGHOLD)),
    ABANDONED_CAMP("Abandoned Camp", new GUIMaterial(Material.CAMPFIRE), List.of(
            KatzeStructure.ABANDONED_CAMP_BAMBOO_JUNGLE, KatzeStructure.ABANDONED_CAMP_BIRCH_FOREST, KatzeStructure.ABANDONED_CAMP_CHERRY_GROVE,
            KatzeStructure.ABANDONED_CAMP_DAPPLED_FOREST, KatzeStructure.ABANDONED_CAMP_FLOWER_FOREST, KatzeStructure.ABANDONED_CAMP_FOREST,
            KatzeStructure.ABANDONED_CAMP_MEADOW, KatzeStructure.ABANDONED_CAMP_OLD_GROWTH_BIRCH_FOREST, KatzeStructure.ABANDONED_CAMP_OLD_GROWTH_PINE_TAIGA,
            KatzeStructure.ABANDONED_CAMP_OLD_GROWTH_SPRUCE_TAIGA, KatzeStructure.ABANDONED_CAMP_PALE_GARDEN, KatzeStructure.ABANDONED_CAMP_SAVANNA,
            KatzeStructure.ABANDONED_CAMP_SNOWY_TAIGA, KatzeStructure.ABANDONED_CAMP_SPARSE_JUNGLE, KatzeStructure.ABANDONED_CAMP_SWAMP,
            KatzeStructure.ABANDONED_CAMP_TAIGA, KatzeStructure.ABANDONED_CAMP_WINDSWEPT_FOREST, KatzeStructure.ABANDONED_CAMP_WOODED_BADLANDS));

    private final String name;
    private final GUIMaterial guiMaterial;
    private final List<KatzeStructure> structures;

    StructureSimplified(String name, GUIMaterial guiMaterial, List<KatzeStructure> structures) {
        this.name = name;
        this.guiMaterial = guiMaterial;
        this.structures = structures;
    }
}

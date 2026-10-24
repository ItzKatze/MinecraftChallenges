package gg.itzkatze.katze.data;

import gg.itzkatze.katze.gui.GUIMaterial;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.List;

@Getter
public enum BiomeSimplified {
    OCEAN("Ocean", new GUIMaterial(Material.WATER_BUCKET), List.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.FROZEN_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
            Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN)),
    PLAINS("Plains", new GUIMaterial(Material.GRASS_BLOCK), List.of(Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.SNOWY_PLAINS)),
    DESERT("Desert", new GUIMaterial(Material.SAND), List.of(Biome.DESERT)),
    BIRCH_FOREST("Birch Forest", new GUIMaterial(Material.BIRCH_LOG), List.of(Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST)),
    OAK_FOREST("Oak Forest", new GUIMaterial(Material.OAK_LOG), List.of(Biome.FOREST, Biome.FLOWER_FOREST)),
    DAPPLED_FOREST("Dappled Forest", new GUIMaterial(Material.POPLAR_LOG), List.of(Biome.DAPPLED_FOREST)),
    JUNGLE("Jungle", new GUIMaterial(Material.JUNGLE_LOG), List.of(Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.BAMBOO_JUNGLE)),
    TAIGA("Taiga", new GUIMaterial(Material.SPRUCE_LOG), List.of(Biome.TAIGA, Biome.SNOWY_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.OLD_GROWTH_PINE_TAIGA)),
    SAVANNA("Savanna", new GUIMaterial(Material.ACACIA_LOG), List.of(Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.WINDSWEPT_SAVANNA)),
    ROOFED_FOREST("Roofed Forest", new GUIMaterial(Material.DARK_OAK_LOG), List.of(Biome.DARK_FOREST, Biome.PALE_GARDEN)),
    SWAMP("Swamp", new GUIMaterial(Material.MANGROVE_LOG), List.of(Biome.SWAMP, Biome.MANGROVE_SWAMP)),
    BADLANDS("Badlands", new GUIMaterial(Material.TERRACOTTA), List.of(Biome.BADLANDS, Biome.WOODED_BADLANDS, Biome.ERODED_BADLANDS)),
    MOUNTAINS("Mountains", new GUIMaterial(Material.STONE), List.of(Biome.WINDSWEPT_HILLS, Biome.WINDSWEPT_FOREST, Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.MEADOW,
            Biome.GROVE, Biome.SNOWY_SLOPES, Biome.FROZEN_PEAKS, Biome.JAGGED_PEAKS, Biome.STONY_PEAKS)),
    RIVER("River", new GUIMaterial(Material.WATER_BUCKET), List.of(Biome.RIVER, Biome.FROZEN_RIVER)),
    BEACH("Beach", new GUIMaterial(Material.SAND), List.of(Biome.BEACH, Biome.SNOWY_BEACH, Biome.STONY_SHORE)),
    CAVE("Cave", new GUIMaterial(Material.SMALL_DRIPLEAF), List.of(Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES, Biome.DEEP_DARK, Biome.SULFUR_CAVES)),
    NETHER_WASTES("Nether Wastes", new GUIMaterial(Material.NETHERRACK), List.of(Biome.NETHER_WASTES)),
    SOUL_SAND_VALLEY("Soul Sand Valley", new GUIMaterial(Material.SOUL_SAND), List.of(Biome.SOUL_SAND_VALLEY)),
    CRIMSON_FOREST("Crimson Forest", new GUIMaterial(Material.CRIMSON_HYPHAE), List.of(Biome.CRIMSON_FOREST)),
    WARPED_FOREST("Warped Forest", new GUIMaterial(Material.WARPED_HYPHAE), List.of(Biome.WARPED_FOREST)),
    BASALT_DELTAS("Basalt Deltas", new GUIMaterial(Material.BASALT), List.of(Biome.BASALT_DELTAS)),
    END("The End", new GUIMaterial(Material.END_STONE), List.of(Biome.THE_END, Biome.SMALL_END_ISLANDS, Biome.END_MIDLANDS, Biome.END_HIGHLANDS, Biome.END_BARRENS)),
    FROZEN("Frozen", new GUIMaterial(Material.ICE), List.of(Biome.ICE_SPIKES, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.FROZEN_PEAKS, Biome.FROZEN_RIVER)),
    MUSHROOM_FIELDS("Mushroom Fields", new GUIMaterial(Material.RED_MUSHROOM_BLOCK), List.of(Biome.MUSHROOM_FIELDS)),
    ;

    private final String name;
    private final GUIMaterial guiMaterial;
    private final List<Biome> biomes;

    BiomeSimplified(String name, GUIMaterial guiMaterial, List<Biome> biomes) {
        this.name = name;
        this.guiMaterial = guiMaterial;
        this.biomes = biomes;
    }
}

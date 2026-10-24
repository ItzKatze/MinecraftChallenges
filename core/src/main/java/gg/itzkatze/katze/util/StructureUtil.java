package gg.itzkatze.katze.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureUtil {

    private static final String NAMESPACE = "katze";
    private static final Map<String, Scan> SCANS = new HashMap<>();

    private record Scan(List<BlockVector> blockEntityPositions, @Nullable BoundingBox barrierBounds) {
    }

    public static @NotNull List<BlockVector> blockEntityPositions(@NotNull String name, @NotNull Structure structure) {
        return scan(name, structure).blockEntityPositions();
    }

    public static @Nullable BoundingBox barrierBounds(@NotNull String name, @NotNull Structure structure) {
        return scan(name, structure).barrierBounds();
    }

    private static @NotNull Scan scan(@NotNull String name, @NotNull Structure structure) {
        return SCANS.computeIfAbsent(name, key -> {
            List<BlockVector> blockEntities = new ArrayList<>();
            int[] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
            int[] max = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            Optional<Palette> palette = structure.getPalettes().stream().findFirst();
            for (BlockState block : palette.map(Palette::getBlocks).orElse(List.of())) {
                if (block instanceof TileState) {
                    blockEntities.add(new BlockVector(block.getX(), block.getY(), block.getZ()));
                }
                if (block.getType() == Material.BARRIER) {
                    min[0] = Math.min(min[0], block.getX());
                    min[1] = Math.min(min[1], block.getY());
                    min[2] = Math.min(min[2], block.getZ());
                    max[0] = Math.max(max[0], block.getX());
                    max[1] = Math.max(max[1], block.getY());
                    max[2] = Math.max(max[2], block.getZ());
                }
            }
            BoundingBox barriers = min[0] > max[0] ? null : new BoundingBox(min[0], min[1], min[2], max[0] + 1, max[1] + 1, max[2] + 1);
            return new Scan(List.copyOf(blockEntities), barriers);
        });
    }

    public static @Nullable Structure loadStructure(@NotNull String name) {
        StructureManager manager = Bukkit.getStructureManager();
        NamespacedKey key = new NamespacedKey(NAMESPACE, name);
        Structure cached = manager.getStructure(key);
        if (cached != null) {
            return cached;
        }

        try (InputStream in = StructureUtil.class.getResourceAsStream("/structures/" + name + ".nbt")) {
            if (in == null) {
                Bukkit.getLogger().severe("Could not find structure file: " + name);
                return null;
            }
            Structure structure = manager.loadStructure(in);
            manager.registerStructure(key, structure);
            return structure;
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not load structure " + name + ": " + e.getMessage());
            return null;
        }
    }
}

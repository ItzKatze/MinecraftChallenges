package gg.itzkatze.katze.util;

import gg.itzkatze.katze.KatzeGames;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorldStorage {

    private static final String DATA_DIR_NAME = "katze_data";

    private WorldStorage() {
    }

    public static Path getFilePath(String fileName) {
        World mainWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        File baseDir = (mainWorld != null) ? mainWorld.getWorldFolder() : KatzeGames.get().getDataDirectory();
        File dataDir = new File(baseDir, DATA_DIR_NAME);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new File(dataDir, fileName).toPath();
    }

    public static <T> void save(String fileName, T object) {
        Path path = getFilePath(fileName);
        try {
            JsonFiles.write(path, object);
        } catch (IOException e) {
            KatzeGames.get().getLogger().severe("Failed to save " + fileName + ": " + e.getMessage());
        }
    }

    public static <T> T load(String fileName, Class<T> clazz) {
        Path path = getFilePath(fileName);
        try {
            return JsonFiles.read(path, clazz);
        } catch (Exception e) {
            KatzeGames.get().getLogger().warning("Failed to load " + fileName + ", starting fresh: " + e.getMessage());
            return null;
        }
    }

    public static boolean exists(String fileName) {
        return Files.exists(getFilePath(fileName));
    }

    public static void delete(String fileName) {
        Path path = getFilePath(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}

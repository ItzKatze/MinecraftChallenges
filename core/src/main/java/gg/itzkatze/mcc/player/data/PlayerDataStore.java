package gg.itzkatze.mcc.player.data;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.util.JsonFiles;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerDataStore {

    public interface Delayer {
        void runLater(@NotNull Runnable task, long ticks);
    }

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges/" + PlayerDataStore.class.getSimpleName());
    private static final String FILE_NAME = "data/players.json";
    private static final int FILE_VERSION = 1;
    private static final long WRITE_DELAY_TICKS = 100;
    private static final DateTimeFormatter QUARANTINE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static @Nullable PlayerDataStore installed;

    private final Path file;
    private final Delayer delayer;
    private final Map<UUID, PlayerProfile> profiles = new LinkedHashMap<>();
    private boolean dirty;
    private boolean writePending;
    private int writes;

    public PlayerDataStore(@NotNull Path file, @NotNull Delayer delayer) {
        this.file = file;
        this.delayer = delayer;
        readFile();
    }

    public static @NotNull PlayerDataStore get() {
        if (installed == null) {
            throw new IllegalStateException("The player data has not been loaded");
        }
        return installed;
    }

    public static void install(@Nullable PlayerDataStore store) {
        installed = store;
    }

    public static void load() {
        MinecraftChallenges plugin = MinecraftChallenges.get();
        Path file = new File(plugin.getDataDirectory(), FILE_NAME).toPath();
        install(new PlayerDataStore(file, (task, ticks) -> Bukkit.getScheduler().runTaskLater(plugin, task, ticks)));
    }

    public static void flushInstalled() {
        if (installed != null) {
            installed.flush();
        }
    }

    public @Nullable PlayerProfile find(@NotNull UUID player) {
        return profiles.get(player);
    }

    public @NotNull PlayerProfile profile(@NotNull UUID player, @Nullable String name) {
        PlayerProfile profile = profiles.computeIfAbsent(player, key -> new PlayerProfile());
        profile.rename(name);
        return profile;
    }

    public @NotNull @UnmodifiableView Map<UUID, PlayerProfile> profiles() {
        return Collections.unmodifiableMap(profiles);
    }

    public @NotNull Optional<UUID> findByName(@NotNull String name) {
        return profiles.entrySet().stream()
                .filter(entry -> name.equalsIgnoreCase(entry.getValue().name()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public void markDirty() {
        dirty = true;
        if (!writePending) {
            writePending = true;
            delayer.runLater(this::writeScheduled, WRITE_DELAY_TICKS);
        }
    }

    public void flush() {
        write();
    }

    boolean isDirty() {
        return dirty;
    }

    int writes() {
        return writes;
    }

    private void writeScheduled() {
        writePending = false;
        write();
    }

    private void write() {
        if (!dirty) return;

        FileData data = new FileData();
        profiles.forEach((id, profile) -> data.players.put(id.toString(), profile));
        try {
            JsonFiles.write(file, data);
            dirty = false;
            writes++;
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to save {}: {}", file, e.getMessage());
        }
    }

    private void readFile() {
        try {
            FileData data = JsonFiles.read(file, FileData.class);
            if (data != null && data.players != null) {
                data.players.forEach(this::adopt);
            }
        } catch (IOException | RuntimeException e) {
            profiles.clear();
            quarantine(e);
        }
    }

    private void adopt(@NotNull String key, @Nullable PlayerProfile profile) {
        if (profile == null) return;

        UUID id;
        try {
            id = UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            logger.warn("Ignoring the entry \"{}\" of {}: it is not a UUID.", key, file);
            return;
        }
        profile.normalize();
        profiles.put(id, profile);
    }

    private void quarantine(@NotNull Exception cause) {
        Path aside = file.resolveSibling(file.getFileName() + ".corrupt-" + LocalDateTime.now().format(QUARANTINE_TIME));
        try {
            Files.move(file, aside);
            logger.warn("Could not read {} ({}). Moved it to {} and started with empty player data.", file, cause.getMessage(), aside.getFileName());
        } catch (IOException e) {
            logger.error("Could not read {} ({}) and could not move it aside ({}). Starting with empty player data.", file, cause.getMessage(), e.getMessage());
        }
    }

    private static final class FileData {
        private int version = FILE_VERSION;
        private Map<String, PlayerProfile> players = new LinkedHashMap<>();
    }
}

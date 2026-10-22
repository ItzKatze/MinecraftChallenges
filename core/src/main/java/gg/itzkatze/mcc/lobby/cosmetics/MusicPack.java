package gg.itzkatze.mcc.lobby.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.mcc.MinecraftChallenges;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The resource pack that names each soundtrack song as a sound of its own. It holds no audio, only a sounds.json pointing at the game's files, and the same songs always
 * give the same bytes (fixed entry order and time, stored uncompressed), so the hash of an uploaded copy stays valid until the song list changes.
 */
public final class MusicPack {

    public static final UUID PACK_ID = UUID.nameUUIDFromBytes("minecraftchallenges:music-pack".getBytes(StandardCharsets.UTF_8));

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges/" + MusicPack.class.getSimpleName());
    private static final String MCMETA = "{\"pack\": {\"description\": \"MinecraftChallenges music\", \"min_format\": 97, \"max_format\": 98}}";
    private static final String SOUNDS = "assets/minecraftchallenges/sounds.json";
    private static final LocalDateTime ENTRY_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final Component PROMPT = Component.text("Adds the C418 soundtrack songs as Jump & Run music. Optional.", NamedTextColor.GRAY);

    private static @Nullable MusicPack installed;

    private final byte[] bytes;
    private final String sha1;
    private final @Nullable URI url;
    private final Set<UUID> loaded = new HashSet<>();

    public MusicPack(@NotNull List<MusicCosmetic> songs, @NotNull Path file, @Nullable String url) {
        List<MusicCosmetic> packed = songs.stream().filter(song -> song.packFile() != null).toList();
        this.bytes = zip(packed);
        this.sha1 = HexFormat.of().formatHex(digest(bytes));
        write(file, bytes);
        this.url = parse(url);
        logger.info("Music pack: {} ({} songs, SHA-1 {}).", display(file), packed.size(), sha1);
        if (this.url == null) {
            logger.info("Upload music-pack.zip somewhere players can download it directly and set music-pack-url in config.yml; until then the soundtrack songs stay silent.");
        } else {
            logger.info("Sending the music pack from {}.", this.url);
        }
    }

    public static void load() {
        MinecraftChallenges plugin = MinecraftChallenges.get();
        install(new MusicPack(List.of(MusicCosmetic.values()), plugin.getDataFolder().toPath().resolve("music-pack.zip"),
                plugin.getConfig().getString("music-pack-url", "")));
    }

    public static @NotNull MusicPack get() {
        if (installed == null) {
            throw new IllegalStateException("The music pack has not been loaded");
        }
        return installed;
    }

    public static void install(@Nullable MusicPack pack) {
        installed = pack;
    }

    public byte @NotNull [] bytes() {
        return bytes.clone();
    }

    public @NotNull String sha1() {
        return sha1;
    }

    public @Nullable URI url() {
        return url;
    }

    public @Nullable ResourcePackRequest request() {
        if (url == null) return null;
        return ResourcePackRequest.resourcePackRequest()
                .packs(ResourcePackInfo.resourcePackInfo(PACK_ID, url, sha1))
                .required(false)
                .replace(false)
                .prompt(PROMPT)
                .build();
    }

    public boolean isLoaded(@NotNull UUID player) {
        return loaded.contains(player);
    }

    // ACCEPTED and DOWNLOADED are still pending, so only SUCCESSFULLY_LOADED counts as loaded.
    public void status(@NotNull UUID player, @NotNull String name, @NotNull PlayerResourcePackStatusEvent.Status status) {
        if (status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            loaded.add(player);
            return;
        }
        loaded.remove(player);
        if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || status == PlayerResourcePackStatusEvent.Status.INVALID_URL) {
            logger.warn("{} could not load the music pack from {} ({}). Check that the link is a direct download of the current music-pack.zip (SHA-1 {}).", name, url, status,
                    sha1);
        }
    }

    public void forget(@NotNull UUID player) {
        loaded.remove(player);
    }

    private static byte[] zip(@NotNull List<MusicCosmetic> songs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            entry(zip, "pack.mcmeta", MCMETA.getBytes(StandardCharsets.UTF_8));
            entry(zip, SOUNDS, sounds(songs).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static void entry(@NotNull ZipOutputStream zip, @NotNull String name, byte @NotNull [] data) throws IOException {
        CRC32 crc = new CRC32();
        crc.update(data);
        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        entry.setCrc(crc.getValue());
        entry.setTimeLocal(ENTRY_TIME);
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }

    private static @NotNull String sounds(@NotNull List<MusicCosmetic> songs) {
        JsonObject sounds = new JsonObject();
        for (MusicCosmetic song : songs) {
            JsonObject sound = new JsonObject();
            sound.addProperty("name", "minecraft:" + song.packFile());
            sound.addProperty("stream", true);
            JsonArray list = new JsonArray();
            list.add(sound);
            JsonObject event = new JsonObject();
            event.add("sounds", list);
            sounds.add(song.sound().name().value(), event);
        }
        return new Gson().toJson(sounds);
    }

    private static byte @NotNull [] digest(byte @NotNull [] data) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void write(@NotNull Path file, byte @NotNull [] bytes) {
        try {
            if (Files.isRegularFile(file) && Arrays.equals(Files.readAllBytes(file), bytes)) return;
            Path folder = file.toAbsolutePath().getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.write(file, bytes);
        } catch (IOException e) {
            logger.error("Could not write {}.", display(file), e);
        }
    }

    // The 26.3 client refuses any link that is not http or https (INVALID_URL), so such a link counts as none from the start.
    private static @Nullable URI parse(@Nullable String raw) {
        String link = raw == null ? "" : raw.trim();
        if (link.isEmpty()) return null;
        try {
            URI uri = new URI(link);
            if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                return uri;
            }
        } catch (URISyntaxException ignored) {
        }
        logger.error("music-pack-url \"{}\" is not a valid http or https link, so the music pack is not sent.", link);
        return null;
    }

    private static @NotNull String display(@NotNull Path file) {
        return file.toString().replace('\\', '/');
    }
}

package gg.itzkatze.mcc.lobby.cosmetics;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

// The client mod decodes these messages with FriendlyByteBuf, so any change to their format needs a new channel name.
public final class MusicChannel {

    public static final String NAME = "minecraftchallenges:music";

    private static final int PLAY = 0;
    private static final int FADE = 1;
    private static final int STOP = 2;
    private static final int SOUND_EVENT = 0;
    private static final int GAME_FILE = 1;

    private MusicChannel() {
    }

    public static void register(@NotNull Plugin plugin) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, NAME);
    }

    public static boolean isListening(@Nullable Player player) {
        return player != null && player.getListeningPluginChannels().contains(NAME);
    }

    public static void play(@NotNull Plugin plugin, @NotNull Player player, @NotNull MusicCosmetic music) {
        ByteArrayOutputStream message = message(PLAY);
        if (music.isSong()) {
            writeString(message, "minecraft:" + music.file());
            message.write(GAME_FILE);
        } else {
            writeString(message, music.sound().name().asString());
            message.write(SOUND_EVENT);
        }
        send(plugin, player, message);
    }

    public static void fade(@NotNull Plugin plugin, @NotNull Player player, int ticks) {
        ByteArrayOutputStream message = message(FADE);
        writeVarInt(message, ticks);
        send(plugin, player, message);
    }

    public static void stop(@NotNull Plugin plugin, @NotNull Player player) {
        send(plugin, player, message(STOP));
    }

    private static @NotNull ByteArrayOutputStream message(int kind) {
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(kind);
        return message;
    }

    private static void send(@NotNull Plugin plugin, @NotNull Player player, @NotNull ByteArrayOutputStream message) {
        player.sendPluginMessage(plugin, NAME, message.toByteArray());
    }

    private static void writeVarInt(@NotNull ByteArrayOutputStream message, int value) {
        while ((value & ~0x7F) != 0) {
            message.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        message.write(value);
    }

    private static void writeString(@NotNull ByteArrayOutputStream message, @NotNull String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(message, bytes.length);
        message.writeBytes(bytes);
    }
}

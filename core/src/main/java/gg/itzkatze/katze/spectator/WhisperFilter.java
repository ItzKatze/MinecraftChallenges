package gg.itzkatze.katze.spectator;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

public final class WhisperFilter {

    public static final Component ONLY_SPECTATORS = Component.text("During the battle you can only message other spectators.", KatzeColor.RED);
    public static final Component ONLY_SPECTATOR_CHAT = Component.text("During the battle spectators can only write in the spectator chat.", KatzeColor.RED);

    private static final String NAMESPACE = "minecraft:";
    private static final Set<String> WHISPERS = Set.of("msg", "tell", "w");
    private static final Set<String> CHAT_COMMANDS = Set.of("me", "teammsg", "tm");

    private WhisperFilter() {
    }

    public static @Nullable Component refusal(@NotNull String commandLine, @NotNull Predicate<String> isOnlineSpectator) {
        String[] words = commandLine.trim().split("\\s+");
        String label = label(words[0]);
        if (CHAT_COMMANDS.contains(label)) {
            return ONLY_SPECTATOR_CHAT;
        }
        if (!WHISPERS.contains(label) || words.length < 2) {
            return null;
        }
        String target = words[1];
        return target.startsWith("@") || !isOnlineSpectator.test(target) ? ONLY_SPECTATORS : null;
    }

    private static @NotNull String label(@NotNull String word) {
        String label = (word.startsWith("/") ? word.substring(1) : word).toLowerCase(Locale.ROOT);
        return label.startsWith(NAMESPACE) ? label.substring(NAMESPACE.length()) : label;
    }
}

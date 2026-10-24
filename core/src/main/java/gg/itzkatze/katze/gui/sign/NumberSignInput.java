package gg.itzkatze.katze.gui.sign;

import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.function.BiConsumer;

public final class NumberSignInput {

    private NumberSignInput() {
    }

    public static void open(
            KatzePlayer player,
            String label,
            int current,
            int min,
            int max,
            BiConsumer<KatzePlayer, Integer> onComplete,
            BiConsumer<KatzePlayer, String> onError
    ) {
        SignInput.open(
                player,
                List.of(
                        Component.empty(),
                        Component.text(label).color(NamedTextColor.AQUA),
                        Component.text("Current: " + current).color(NamedTextColor.GRAY),
                        Component.text("Min " + min + " Max " + max).color(NamedTextColor.YELLOW)
                ),
                (katzePlayer, lines) -> {
                    String raw = lines.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .findFirst()
                            .orElse("");
                    if (raw.isEmpty()) {
                        if (onError != null) {
                            onError.accept(katzePlayer, "No value entered.");
                        }
                        return;
                    }
                    int value;
                    try {
                        value = Integer.parseInt(raw);
                    } catch (NumberFormatException e) {
                        if (onError != null) {
                            onError.accept(katzePlayer, "Invalid number.");
                        }
                        return;
                    }

                    int clamped = Math.max(min, Math.min(max, value));
                    if (value != clamped && onError != null) {
                        onError.accept(katzePlayer, "Clamped to " + clamped + ".");
                    }
                    onComplete.accept(katzePlayer, clamped);
                }
        );
    }
}

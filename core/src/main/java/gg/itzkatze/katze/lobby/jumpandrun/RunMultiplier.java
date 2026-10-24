package gg.itzkatze.katze.lobby.jumpandrun;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RunMultiplier {

    private static final int BLOCKS_PER_LEVEL = 50;
    private static final double BONUS_PER_LEVEL = 0.1;

    private static final class RunState {
        private int announcedLevel;
        private double bonusRemainder;
    }

    private final Map<UUID, RunState> states = new HashMap<>();

    public int awardLanding(Player player, int score) {
        UUID id = player.getUniqueId();
        RunState state = states.computeIfAbsent(id, key -> new RunState());

        int level = Math.max(0, score) / BLOCKS_PER_LEVEL;
        double multiplier = 1.0 + level * BONUS_PER_LEVEL;

        if (level > state.announcedLevel) {
            state.announcedLevel = level;

            player.sendMessage(
                    Component.text("Multiplier increased to ", KatzeColor.LIME)
                            .append(Component.text(
                                    String.format(java.util.Locale.US, "%.1f", multiplier) + "x",
                                    KatzeColor.GOLD
                            ))
                            .append(Component.text("!", KatzeColor.LIME))
            );
        }

        state.bonusRemainder += multiplier - 1.0;

        int bonusPoints = (int) state.bonusRemainder;
        state.bonusRemainder -= bonusPoints;

        return 1 + bonusPoints;
    }

    public void reset(UUID player) {
        states.remove(player);
    }
}

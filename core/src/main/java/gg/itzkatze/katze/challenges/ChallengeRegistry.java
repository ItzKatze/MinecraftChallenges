package gg.itzkatze.katze.challenges;

import gg.itzkatze.katze.KatzeGames;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public class ChallengeRegistry {

    private static final Map<ChallengeType, Challenge> challenges = new HashMap<>();

    static {
        ServiceLoader.load(Challenge.class).forEach(ChallengeRegistry::register);
        KatzeGames.loopThroughPackage("gg.itzkatze.katze", Challenge.class)
                .forEach(ChallengeRegistry::register);
    }

    private static void register(Challenge challenge) {
        if (challenge == null) return;
        challenges.putIfAbsent(challenge.getType(), challenge);
    }

    public static Set<Challenge> getAllChallenges() {
        return Set.copyOf(challenges.values());
    }

    public static Challenge getChallenge(@NotNull ChallengeType type) {
        return challenges.get(type);
    }

    public static boolean hasChallenge(@NotNull String name) {
        return challenges.values().stream().anyMatch(c -> c.getType().name().equalsIgnoreCase(name));
    }
}

package gg.itzkatze.mcc.lobby.cosmetics.cases;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.lobby.Lobby;
import gg.itzkatze.mcc.lobby.LobbyManager;
import gg.itzkatze.mcc.lobby.cosmetics.Rarity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class CaseFireworks {

    private static final NamespacedKey TAG = new NamespacedKey("mcc", "case_firework");
    private static final Sound LAUNCH = Sound.sound(Key.key("entity.firework_rocket.launch"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound BLAST = Sound.sound(Key.key("entity.firework_rocket.blast"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound LARGE_BLAST = Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound CRACKLE = Sound.sound(Key.key("entity.firework_rocket.twinkle"), Sound.Source.PLAYER, 1f, 1f);
    private static final int FIRST_ROCKET = 6;
    private static final int ROCKET_GAP = 6;
    private static final int CRACKLE_DELAY = 15;
    private static final double ABOVE_CASE = 2.5;
    private static final double AHEAD_OF_PLAYER = 3;

    private CaseFireworks() {
    }

    // The rockets are silent and shown to the opener only: a rocket's own launch sound reaches every player nearby, so the opener gets these sounds instead.
    public static void play(@NotNull Player player, @NotNull Rarity tier) {
        player.playSound(LAUNCH);
        UUID id = player.getUniqueId();
        List<List<FireworkEffect>> rockets = rockets(tier);
        for (int rocket = 0; rocket < rockets.size(); rocket++) {
            List<FireworkEffect> effects = rockets.get(rocket);
            later(id, FIRST_ROCKET + rocket * ROCKET_GAP, online -> launch(online, effects));
        }
        if (rockets.stream().flatMap(List::stream).anyMatch(FireworkEffect::hasFlicker)) {
            later(id, FIRST_ROCKET + (rockets.size() - 1) * ROCKET_GAP + CRACKLE_DELAY, online -> online.playSound(CRACKLE));
        }
    }

    static boolean isCaseFirework(@Nullable Entity entity) {
        return entity instanceof Firework && entity.getPersistentDataContainer().has(TAG);
    }

    private static void later(@NotNull UUID id, int ticks, @NotNull Consumer<Player> action) {
        Bukkit.getScheduler().runTaskLater(MinecraftChallenges.get(), () -> {
            Player online = Bukkit.getPlayer(id);
            if (online != null) action.accept(online);
        }, ticks);
    }

    private static void launch(@NotNull Player player, @NotNull List<FireworkEffect> effects) {
        Location at = burstPoint(player);
        Firework rocket = at.getWorld().spawn(at, Firework.class, firework -> {
            firework.setVisibleByDefault(false);
            firework.setPersistent(false);
            firework.setSilent(true);
            firework.getPersistentDataContainer().set(TAG, PersistentDataType.BOOLEAN, true);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffects(effects);
            firework.setFireworkMeta(meta);
        });
        player.showEntity(MinecraftChallenges.get(), rocket);
        rocket.detonate();
        boolean large = effects.stream().anyMatch(effect -> effect.getType() == FireworkEffect.Type.BALL_LARGE);
        player.playSound(large ? LARGE_BLAST : BLAST, at.getX(), at.getY(), at.getZ());
    }

    // Above the case the player stands at, or in front of them when there is none (a roll that ends after /start, an island without a case).
    private static @NotNull Location burstPoint(@NotNull Player player) {
        Location chest = nearestCase(player);
        if (chest != null) return chest.add(0.5, ABOVE_CASE, 0.5);

        Location eye = player.getEyeLocation();
        return eye.add(eye.getDirection().multiply(AHEAD_OF_PLAYER));
    }

    private static @Nullable Location nearestCase(@NotNull Player player) {
        Lobby lobby = LobbyManager.getActiveLobby();
        if (lobby == null) return null;

        Location feet = player.getLocation();
        return lobby.getFeatures().stream()
                .filter(CaseSpot.class::isInstance)
                .map(feature -> ((CaseSpot) feature).getChestLocation())
                .filter(chest -> chest != null && feet.getWorld().equals(chest.getWorld()))
                .min(Comparator.comparingDouble(feet::distanceSquared))
                .orElse(null);
    }

    private static @NotNull List<List<FireworkEffect>> rockets(@NotNull Rarity tier) {
        Color color = Color.fromRGB(tier.color().value());
        return switch (tier) {
            case COMMON -> List.of(List.of(effect(FireworkEffect.Type.BALL, color, false, false)));
            case UNCOMMON -> List.of(List.of(effect(FireworkEffect.Type.BALL, color, true, false)));
            case RARE -> List.of(List.of(effect(FireworkEffect.Type.BALL_LARGE, color, false, false)));
            case EPIC -> List.of(List.of(effect(FireworkEffect.Type.BALL_LARGE, color, true, false)));
            case LEGENDARY -> List.of(List.of(effect(FireworkEffect.Type.BALL_LARGE, color, true, true), effect(FireworkEffect.Type.STAR, color, true, true)));
            case MYTHIC -> List.of(List.of(effect(FireworkEffect.Type.BALL_LARGE, color, true, true)), List.of(effect(FireworkEffect.Type.STAR, color, true, true)),
                    List.of(effect(FireworkEffect.Type.BURST, color, true, true)));
        };
    }

    private static @NotNull FireworkEffect effect(@NotNull FireworkEffect.Type type, @NotNull Color color, boolean trail, boolean crackle) {
        return FireworkEffect.builder().with(type).withColor(color).withFade(Color.WHITE).trail(trail).flicker(crackle).build();
    }
}

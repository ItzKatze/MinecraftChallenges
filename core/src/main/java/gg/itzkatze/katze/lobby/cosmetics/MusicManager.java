package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MusicManager {

    private static final NamespacedKey EMITTER_TAG = new NamespacedKey("katze", "music_emitter");
    private static final Set<UUID> SILENCED = new HashSet<>();
    private static final int FADE_TICKS = 60;
    private static final int SILENCE_TICKS = 20;
    // The client lowers a mono sound at volume 1 linearly with the distance to its emitter, down to silence at 16 blocks.
    private static final double SILENT_DISTANCE = 16;
    private static final double OUT_OF_EARSHOT = SILENT_DISTANCE + 8;

    private final Plugin plugin;
    private final Map<UUID, Playback> playbacks = new HashMap<>();
    private final Set<UUID> modPlaybacks = new HashSet<>();

    public MusicManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isEmitter(Entity entity) {
        return entity.getPersistentDataContainer().has(EMITTER_TAG);
    }

    // The game saves the silent flag with the player, so it must never outlive the plugin.
    public static void releaseAll() {
        for (UUID id : SILENCED) {
            Player online = Bukkit.getPlayer(id);
            if (online != null) {
                online.setSilent(false);
            }
        }
        SILENCED.clear();
    }

    public void start(Player player) {
        stop(player);

        UUID id = player.getUniqueId();
        boolean mod = MusicChannel.isListening(player);

        CosmeticStore.get().equippedMusic(id)
                .filter(music -> mod || !music.isSong())
                .ifPresent(music -> {
                    if (mod) {
                        MusicChannel.play(plugin, player, music);
                        modPlaybacks.add(id);
                        return;
                    }
                    Playback playback = new Playback(id, music, spawnEmitter(player));
                    playback.runTaskTimer(plugin, 1L, 1L);
                    playbacks.put(id, playback);
                    playback.play(player);
                });
    }

    public void fadeOut(Player player) {
        if (modPlaybacks.remove(player.getUniqueId())) {
            MusicChannel.fade(plugin, player, FADE_TICKS);
            return;
        }
        Playback playback = playbacks.get(player.getUniqueId());
        if (playback == null || player.isSilent()) {
            stop(player);
            return;
        }
        playback.fading = true;
    }

    public void stop(Player player) {
        if (modPlaybacks.remove(player.getUniqueId())) {
            MusicChannel.stop(plugin, player);
        }
        end(player.getUniqueId());
        player.stopSound(SoundStop.source(Sound.Source.RECORD));
    }

    public void stopAll() {
        Set<UUID> ids = new HashSet<>(playbacks.keySet());
        ids.addAll(modPlaybacks);
        for (UUID id : ids) {
            Player online = Bukkit.getPlayer(id);
            if (online != null) {
                stop(online);
            } else {
                end(id);
                modPlaybacks.remove(id);
            }
        }
    }

    private void end(UUID id) {
        Playback playback = playbacks.remove(id);
        if (playback == null) return;

        playback.cancel();
        playback.release();
        playback.emitter.remove();
    }

    // Never silent: the client stops a sound bound to an entity that is silent or removed.
    private ItemDisplay spawnEmitter(Player player) {
        ItemDisplay emitter = player.getWorld().spawn(above(player, OUT_OF_EARSHOT), ItemDisplay.class, display -> {
            display.setPersistent(false);
            display.setVisibleByDefault(false);
            display.getPersistentDataContainer().set(EMITTER_TAG, PersistentDataType.BOOLEAN, true);
        });
        player.showEntity(plugin, emitter);
        return emitter;
    }

    private static Location above(Player player, double height) {
        Location eye = player.getEyeLocation();
        return new Location(eye.getWorld(), eye.getX(), eye.getY() + height, eye.getZ());
    }

    private static void stopBackgroundMusic(Player player) {
        player.stopSound(SoundStop.source(Sound.Source.MUSIC));
    }

    // A disc plays twice: bound to the player, who hears it during the run, and bound to the emitter, which follows the player out of earshot.
    // A playing sound can neither move to another entity nor start mid-song, so the emitter's copy takes over when the run ends and fades out.
    private final class Playback extends BukkitRunnable {

        private final UUID id;
        private final MusicCosmetic music;
        private final ItemDisplay emitter;
        private final long length;
        private long ticks;
        private boolean fading;
        private int fadeTicks;
        private double fadeFrom;
        private boolean silenced;

        private Playback(UUID id, MusicCosmetic music, ItemDisplay emitter) {
            this.id = id;
            this.music = music;
            this.emitter = emitter;
            this.length = music.durationSeconds() * 20L;
        }

        private void play(Player player) {
            stopBackgroundMusic(player);
            player.playSound(music.sound(), emitter);
            player.playSound(music.sound(), Sound.Emitter.self());
        }

        @Override
        public void run() {
            Player online = Bukkit.getPlayer(id);
            if (online == null || !online.isOnline()) {
                end(id);
                return;
            }
            if (!emitter.isValid() || !emitter.getWorld().equals(online.getWorld())) {
                stop(online);
                return;
            }

            if (fading) {
                fade(online);
                return;
            }

            follow(online, OUT_OF_EARSHOT);
            if (++ticks >= length) {
                ticks = 0;
                play(online);
                return;
            }

            // The client starts its next background track whenever its own countdown runs out, mid-song too, and the server can neither see nor reset it.
            stopBackgroundMusic(online);
        }

        // The handover runs here, after any teleport that ended the run, so the silent flag and the emitter's move reach the client in the same tick.
        // The player's copy sounds from their feet, an eye height below the listener, so the emitter starts that far above the eyes.
        private void fade(Player online) {
            if (fadeTicks == 0) {
                silence(online);
                fadeFrom = online.getEyeHeight();
            } else if (fadeTicks == SILENCE_TICKS) {
                release();
            }
            if (fadeTicks >= FADE_TICKS) {
                stop(online);
                return;
            }

            // The volume falls linearly with the distance, so this height keeps it at the square of the remaining time, which sounds like an even fade.
            double remaining = 1 - (double) fadeTicks / FADE_TICKS;
            follow(online, SILENT_DISTANCE - (SILENT_DISTANCE - fadeFrom) * remaining * remaining);
            fadeTicks++;
            stopBackgroundMusic(online);
        }

        // The client stops a sound bound to a silent entity, which ends the player's copy alone: both copies share one key and source.
        private void silence(Player player) {
            player.setSilent(true);
            silenced = true;
            SILENCED.add(id);
        }

        private void release() {
            if (!silenced) return;

            silenced = false;
            SILENCED.remove(id);
            Player online = Bukkit.getPlayer(id);
            if (online != null) {
                online.setSilent(false);
            }
        }

        private void follow(Player online, double height) {
            emitter.teleport(above(online, height));
        }
    }
}

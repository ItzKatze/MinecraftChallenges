
package gg.itzkatze.mcc.lobby.cosmetics;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MusicManager {

    private final Plugin plugin;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public MusicManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player player) {
        stop(player);

        UUID id = player.getUniqueId();

        CosmeticStore.get().equippedMusic(id)
                .filter(music -> music.packFile() == null || MusicPack.get().isLoaded(id))
                .ifPresent(music -> {
                    play(player, music);
                    tasks.put(id, Bukkit.getScheduler().runTaskTimer(plugin, new Loop(id, music), 1L, 1L));
                });
    }

    private void play(Player player, MusicCosmetic music) {
        stopBackgroundMusic(player);
        player.playSound(music.sound(), Sound.Emitter.self());
    }

    private static void stopBackgroundMusic(Player player) {
        player.stopSound(SoundStop.source(Sound.Source.MUSIC));
    }

    public void stop(Player player) {
        stop(player.getUniqueId());
        player.stopSound(SoundStop.source(Sound.Source.RECORD));
    }

    private void stop(UUID id) {
        BukkitTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }
    }

    private final class Loop implements Runnable {

        private final UUID id;
        private final MusicCosmetic music;
        private final long length;
        private long ticks;

        private Loop(UUID id, MusicCosmetic music) {
            this.id = id;
            this.music = music;
            this.length = music.durationSeconds() * 20L;
        }

        @Override
        public void run() {
            Player online = Bukkit.getPlayer(id);
            if (online == null || !online.isOnline()) {
                stop(id);
                return;
            }

            if (++ticks >= length) {
                ticks = 0;
                play(online, music);
                return;
            }

            // The client starts its next background track whenever its own countdown runs out, mid-song too, and the server can neither see nor reset it.
            stopBackgroundMusic(online);
        }
    }
}

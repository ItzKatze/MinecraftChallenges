
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

        CosmeticStore.get().equippedMusic(id).ifPresent(music -> {
            play(player, music);
            scheduleNext(id, music);
        });
    }

    private void scheduleNext(UUID id, MusicCosmetic music) {
        long delay = music.durationSeconds() * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    tasks.remove(id);

                    Player online = Bukkit.getPlayer(id);
                    if (online == null || !online.isOnline()) {
                        stop(id);
                        return;
                    }

                    play(online, music);
                    scheduleNext(id, music);
                },
                delay
        );

        tasks.put(id, task);
    }

    private void play(Player player, MusicCosmetic music) {
        player.playSound(music.sound(), net.kyori.adventure.sound.Sound.Emitter.self());
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
}

package gg.itzkatze.mcc.level;

import gg.itzkatze.mcc.MinecraftChallenges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class XpDigest {

    public static final long PERIOD_TICKS = 12_000;

    private XpDigest() {
    }

    public static void start() {
        Bukkit.getScheduler().runTaskTimer(MinecraftChallenges.get(), XpDigest::send, PERIOD_TICKS, PERIOD_TICKS);
    }

    public static void send() {
        LevelStore.get().drainRecentGains().forEach((id, gained) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.sendMessage(Component.text("You gained " + gained + " XP in the last 10 minutes.", NamedTextColor.AQUA));
            }
        });
    }
}

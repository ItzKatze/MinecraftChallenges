package gg.itzkatze.katze.lobby.npc;

import gg.itzkatze.katze.KatzeGames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class NpcDialogue {

    private static final long BASE_DELAY_TICKS = 20;
    private static final long TICKS_PER_CHARACTER = 1;

    private static final Map<UUID, NpcDialogue> running = new HashMap<>();
    private static final Set<Seen> seen = new HashSet<>();

    private final UUID player;
    private final LobbyNpc npc;
    private final List<Component> lines;
    private final Runnable onFinish;
    private int nextLine;
    private @Nullable BukkitTask pending;

    private NpcDialogue(@NotNull UUID player, @NotNull LobbyNpc npc, @NotNull List<Component> lines, @NotNull Runnable onFinish) {
        this.player = player;
        this.npc = npc;
        this.lines = lines;
        this.onFinish = onFinish;
    }

    public static void start(@NotNull Player player, @NotNull LobbyNpc npc, @NotNull List<Component> lines, @NotNull Runnable onFinish) {
        stop(player.getUniqueId());
        NpcDialogue dialogue = new NpcDialogue(player.getUniqueId(), npc, List.copyOf(lines), onFinish);
        running.put(player.getUniqueId(), dialogue);
        dialogue.sendNextLine();
    }

    public static boolean isTalking(@NotNull UUID player, @NotNull String npcId) {
        NpcDialogue dialogue = running.get(player);
        return dialogue != null && dialogue.npc.getId().equals(npcId);
    }

    public static boolean hasSeen(@NotNull UUID player, @NotNull String npcId) {
        return seen.contains(new Seen(npcId, player));
    }

    public static void stop(@NotNull UUID player) {
        NpcDialogue dialogue = running.remove(player);
        if (dialogue != null) {
            dialogue.cancel();
        }
    }

    public static void stopAll(@NotNull String npcId) {
        running.values().removeIf(dialogue -> {
            if (!dialogue.npc.getId().equals(npcId)) return false;
            dialogue.cancel();
            return true;
        });
    }

    static long readingDelay(@NotNull Component line) {
        return BASE_DELAY_TICKS + TICKS_PER_CHARACTER * PlainTextComponentSerializer.plainText().serialize(line).length();
    }

    static @NotNull Component format(@NotNull Component name, @NotNull Component line) {
        return Component.textOfChildren(
                Component.text("[NPC] ", NamedTextColor.YELLOW),
                name,
                Component.text(": ", NamedTextColor.WHITE),
                line.colorIfAbsent(NamedTextColor.WHITE));
    }

    private void sendNextLine() {
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            running.remove(player, this);
            return;
        }

        Component line = lines.get(nextLine++);
        target.sendMessage(format(npc.getName(), line));
        boolean last = nextLine == lines.size();
        if (last) {
            seen.add(new Seen(npc.getId(), player));
        }
        pending = Bukkit.getScheduler().runTaskLater(KatzeGames.get(), last ? this::finish : this::sendNextLine, readingDelay(line));
    }

    private void finish() {
        if (running.remove(player, this) && Bukkit.getPlayer(player) != null) {
            onFinish.run();
        }
    }

    private void cancel() {
        if (pending != null) {
            pending.cancel();
        }
    }

    private record Seen(@NotNull String npcId, @NotNull UUID player) {
    }
}

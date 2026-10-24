package gg.itzkatze.katze.gui.sign;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SignInput implements Listener {

    private static final Map<UUID, ActiveRequest> ACTIVE = new HashMap<>();

    public static void open(KatzePlayer player, List<Component> lines, BiConsumer<KatzePlayer, List<String>> onComplete) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer == null) return;

        closeActive(player.getUuid());

        KatzeGUI currentGui = KatzeGUI.GUI_MAP.get(player.getUuid());
        if (currentGui != null) {
            currentGui.close(player, KatzeGUI.CloseReason.SIGN_OPENED);
        }

        Block target = findSignBlock(bukkitPlayer);
        BlockData originalData = target.getBlockData();
        target.setBlockData(Bukkit.createBlockData(Material.OAK_SIGN), false);

        Sign sign = (Sign) target.getState();
        for (int i = 0; i < 4; i++) {
            Component line = i < lines.size() ? lines.get(i) : Component.empty();
            sign.line(i, line);
        }
        sign.update(false, false);

        ACTIVE.put(player.getUuid(), new ActiveRequest(target, originalData, onComplete));
        bukkitPlayer.openSign(sign, Side.FRONT);
    }

    private static void closeActive(UUID playerId) {
        ActiveRequest active = ACTIVE.remove(playerId);
        if (active == null) return;
        restoreBlock(active.block, active.originalData);
    }

    private static Block findSignBlock(Player player) {
        Block base = player.getLocation().getBlock();
        for (int i = 2; i <= 5; i++) {
            Block candidate = base.getRelative(0, i, 0);
            if (candidate.getType().isAir()) return candidate;
        }
        return base.getRelative(0, 2, 0);
    }

    private static void restoreBlock(Block block, BlockData originalData) {
        if (block == null || originalData == null) return;
        block.setBlockData(originalData, false);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        ActiveRequest active = ACTIVE.get(playerId);
        if (active == null) return;
        if (!event.getBlock().equals(active.block)) return;

        ACTIVE.remove(playerId);

        List<String> lines = new ArrayList<>();
        for (Component component : event.lines()) {
            lines.add(PlainTextComponentSerializer.plainText().serialize(component));
        }

        Bukkit.getScheduler().runTask(KatzeGames.get(), () -> restoreBlock(active.block, active.originalData));
        active.onComplete.accept(KatzePlayer.adapt(event.getPlayer()), lines);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        closeActive(event.getPlayer().getUniqueId());
    }

    private record ActiveRequest(Block block, BlockData originalData,
                                 BiConsumer<KatzePlayer, List<String>> onComplete) {}
}

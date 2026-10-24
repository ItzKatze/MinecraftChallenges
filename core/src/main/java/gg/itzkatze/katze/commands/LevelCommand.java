package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.level.LevelCurve;
import gg.itzkatze.katze.level.LevelProgress;
import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelTag;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.player.data.PlayerDataStore;
import gg.itzkatze.katze.player.data.PlayerProfile;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LevelCommand extends KatzeCommand {

    private record Target(@NotNull UUID id, @NotNull String name) {
    }

    public LevelCommand() {
        super(
                "level",
                List.of(),
                CallerType.NONE,
                "/level [player] | /level add <player> <amount>",
                PermissionDefault.TRUE,
                "Shows a player's level"
        );
    }

    @Override
    protected void executeCommand(CommandCaller caller, String[] args) {
        if (args.length == 0) {
            if (caller instanceof KatzePlayer player) {
                show(caller, new Target(player.getUuid(), player.getName()));
            } else {
                sendUsage(caller);
            }
            return;
        }
        if (args[0].equalsIgnoreCase("add")) {
            add(caller, args);
            return;
        }
        find(args[0]).ifPresentOrElse(target -> show(caller, target), () -> unknown(caller, args[0]));
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String[] args) {
        List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        boolean op = caller.hasPermission(PermissionDefault.OP);
        if (args.length == 1) {
            List<String> options = new ArrayList<>(names);
            if (op) {
                options.add("add");
            }
            return StringUtility.copyPartialMatches(args[0], options, new ArrayList<>());
        }
        if (args.length == 2 && op && args[0].equalsIgnoreCase("add")) {
            return StringUtility.copyPartialMatches(args[1], names, new ArrayList<>());
        }
        return List.of();
    }

    private void add(@NotNull CommandCaller caller, String @NotNull [] args) {
        if (!caller.hasPermission(PermissionDefault.OP)) {
            caller.sendMessage(Component.text("Insufficient permission to execute this command!"));
            return;
        }
        if (args.length != 3) {
            sendUsage(caller);
            return;
        }
        Optional<Target> target = find(args[1]);
        if (target.isEmpty()) {
            unknown(caller, args[1]);
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            caller.sendMessage(Component.text("Please specify a valid number.", KatzeColor.RED));
            return;
        }
        int total = LevelStore.get().adjust(target.get().id(), target.get().name(), amount);
        caller.sendMessage(Component.text(target.get().name() + " now has " + total + " XP (level " + LevelCurve.levelFor(total) + ").", KatzeColor.LIME));
    }

    private static void show(@NotNull CommandCaller caller, @NotNull Target target) {
        LevelProgress progress = LevelStore.get().progress(target.id());
        String next = progress.cost() == 0 ? "max level" : progress.into() + "/" + progress.cost() + " XP to level " + (progress.level() + 1);
        caller.sendMessage(Component.textOfChildren(
                LevelTag.of(progress.level()),
                Component.text(" " + target.name(), KatzeColor.WHITE),
                Component.text(": " + next + ", " + LevelStore.get().xp(target.id()) + " XP in total.", KatzeColor.LIGHT_GRAY)));
    }

    private static void unknown(@NotNull CommandCaller caller, @NotNull String name) {
        caller.sendMessage(Component.text("No player named " + name + " has any XP yet.", KatzeColor.RED));
    }

    private static @NotNull Optional<Target> find(@NotNull String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return Optional.of(new Target(online.getUniqueId(), online.getName()));
        }
        PlayerDataStore data = PlayerDataStore.get();
        return data.findByName(name).map(id -> {
            PlayerProfile profile = data.find(id);
            return new Target(id, profile == null || profile.name() == null ? name : profile.name());
        });
    }
}

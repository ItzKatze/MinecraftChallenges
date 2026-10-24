package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.TabCompletions;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class PointsCommand extends KatzeCommand {

    public PointsCommand() {
        super(
                "points",
                List.of(),
                CallerType.NONE,
                "/points <add|set|remove> <player> <amount>",
                PermissionDefault.OP,
                "Command to add, set or remove points from a player"
        );
    }

    @Override
    protected void executeCommand(final CommandCaller caller, final String[] args) {
        if (args.length < 3) {
            sendUsage(caller);
            return;
        }

        String action = args[0];
        KatzePlayer target = KatzePlayer.adapt(args[1]).orElse(null);

        if (target == null) {
            caller.sendMessage(Component.text("Player couldn't be found.").color(NamedTextColor.RED));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            caller.sendMessage(Component.text("Please specify a valid number.").color(NamedTextColor.RED));
            return;
        }

        if (amount < 0) {
            caller.sendMessage(Component.text("Number must be positive.").color(NamedTextColor.RED));
            return;
        }

        switch (action) {
            case "add" -> target.setPoints(target.getPoints() + amount);
            case "set" -> target.setPoints(amount);
            case "remove" -> target.setPoints(target.getPoints() - amount);
            default -> sendUsage(caller);
        }
    }

    @Override
    protected List<String> tabComplete(final CommandCaller caller, final String[] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("add", "set", "remove"), new ArrayList<>());
        } else if (args.length == 2) {
            return StringUtility.copyPartialMatches(args[1], TabCompletions.getPlayerNamesTabCompletions(), new ArrayList<>());
        } else if (args.length == 3 && args[2].isEmpty()) {
            return List.of("<amount>");
        }
        return List.of();
    }

}

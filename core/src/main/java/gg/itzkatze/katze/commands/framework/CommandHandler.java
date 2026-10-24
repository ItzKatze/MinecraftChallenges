package gg.itzkatze.katze.commands.framework;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private static final Logger logger = LoggerFactory.getLogger("ForceBattle/" + CommandHandler.class.getSimpleName());
    private static final CommandHandler handler = new CommandHandler();

    public static void registerAll(@NotNull Collection<KatzeCommand> commands) {
        commands.forEach(CommandHandler::register);
    }

    public static void register(@NotNull KatzeCommand command) {
        PluginCommand pluginCommand = KatzeGames.get().getCommand(command.getId());

        if (pluginCommand != null) {
            pluginCommand.setExecutor(handler);
            logger.info("Registered command: {}", command.getId());
        }
    }

    private static @NotNull CommandCaller createCaller(CommandSender sender) {
        if (sender instanceof Player player) {
            return KatzePlayer.adapt(player);
        } else if (sender instanceof ConsoleCommandSender) {
            return BukkitConsoleCommandCaller.instance();
        } else if (sender instanceof BlockCommandSender) {
            return BukkitBlockCommandCaller.adapt((BlockCommandSender) sender);
        }
        throw new RuntimeException("Unknown sender type: " + sender.getClass());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NonNull [] args) {
        CommandCaller caller = createCaller(sender);
        KatzeCommand.execute(command.getName(), caller, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NonNull [] args) {
        CommandCaller caller = createCaller(sender);
        return KatzeCommand.tabComplete(command.getName(), caller, args);
    }

}

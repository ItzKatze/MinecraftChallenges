package gg.itzkatze.katze.commands.framework;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;

public class BukkitConsoleCommandCaller implements ConsoleCommandCaller {

    private static BukkitConsoleCommandCaller instance;

    public static BukkitConsoleCommandCaller instance() {
        if (instance == null) {
            instance = new BukkitConsoleCommandCaller();
        }
        return instance;
    }

    @Override
    public CallerType callerType() {
        return CallerType.CONSOLE;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public void sendMessage(Component message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    @Override
    public boolean hasPermission(PermissionDefault permission) {
        return permission.getValue(true);
    }

}

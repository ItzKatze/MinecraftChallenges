package gg.itzkatze.katze.commands.framework;

import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;

public interface CommandCaller {

    CallerType callerType();

    String getName();

    void sendMessage(Component component);

    boolean hasPermission(PermissionDefault permission);

}

package gg.itzkatze.katze.commands.framework;

import net.kyori.adventure.text.Component;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BukkitBlockCommandCaller implements CommandCaller {

    private final BlockCommandSender sender;

    private BukkitBlockCommandCaller(BlockCommandSender sender) {
        this.sender = sender;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull BukkitBlockCommandCaller adapt(BlockCommandSender sender) {
        return new BukkitBlockCommandCaller(sender);
    }

    @Override
    public CallerType callerType() {
        return CallerType.CONSOLE;
    }

    @Override
    public String getName() {
        return "BLOCK";
    }

    @Override
    public void sendMessage(final Component component) {
        sender.sendMessage(component);
    }

    @Override
    public boolean hasPermission(final PermissionDefault permission) {
        return permission.getValue(true);
    }

}

package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.gui.impl.SettingsGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SettingsCommand extends KatzeCommand {

    public SettingsCommand() {
        super(
                "settings",
                List.of("menu"),
                CallerType.PLAYER,
                "/settings",
                PermissionDefault.OP,
                "Command to open the settings menu"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (caller instanceof KatzePlayer player) {
            new SettingsGUI().open(player);
        }
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }

}

package gg.itzkatze.katze.allitems.commands;

import gg.itzkatze.katze.allitems.AllItemsSettings;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AllItemsSettingsCommand extends KatzeCommand {

    public AllItemsSettingsCommand() {
        super(
                "allitemssettings",
                List.of("allitems", "allitemsconfig"),
                CallerType.PLAYER,
                "allitemssettings <team|individual>",
                PermissionDefault.OP,
                "Configure the game mode for All Items challenge"
        );
    }

    @Override
    protected void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (args.length != 1) {
            sendUsage(caller);
            return;
        }

        String mode = args[0].toLowerCase();

        switch (mode) {
            case "team" -> {
                AllItemsSettings.setGameMode(AllItemsSettings.GameMode.TEAM);
                caller.sendMessage(Component.text("All Items mode set to Team mode."));
            }
            case "individual" -> {
                AllItemsSettings.setGameMode(AllItemsSettings.GameMode.INDIVIDUAL);
                caller.sendMessage(Component.text("All Items mode set to Individual mode."));
            }
            default -> {
                caller.sendMessage(Component.text("Invalid mode. Use 'team' or 'individual'."));
                sendUsage(caller);
            }
        }
    }

    @Override
    protected List<String> tabComplete(@NotNull CommandCaller caller, String[] args) {
        if (args.length == 1) {
            return List.of("team", "individual");
        }
        return List.of();
    }
}

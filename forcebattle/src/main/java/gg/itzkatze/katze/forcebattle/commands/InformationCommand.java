package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.forcebattle.gui.info.InfoGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class InformationCommand extends KatzeCommand {

    public InformationCommand() {
        super(
                "information",
                List.of("info", "information"),
                CallerType.PLAYER,
                "/info",
                PermissionDefault.TRUE,
                "Command to open the info gui"
        );
    }

    @Override
    public void executeCommand(CommandCaller caller, String[] args) {
        Optional<KatzePlayer> senderOpt = KatzePlayer.adapt(caller.getName());
        senderOpt.ifPresent(katzePlayer -> new InfoGUI().open(katzePlayer));
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        return List.of();
    }
}

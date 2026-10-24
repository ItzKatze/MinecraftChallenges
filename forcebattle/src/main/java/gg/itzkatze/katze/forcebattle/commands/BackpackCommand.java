package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.forcebattle.ForceBattleSettings;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.forcebattle.ForceBattleGameListener;
import gg.itzkatze.katze.player.KatzePlayer;
import org.jetbrains.annotations.NotNull;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Optional;

public class BackpackCommand extends KatzeCommand {

    public BackpackCommand() {
        super(
                "backpack",
                List.of("bp"),
                CallerType.PLAYER,
                "/backpack",
                PermissionDefault.TRUE,
                "Command to open the backpack"
        );
    }

    @Override
    public void executeCommand(CommandCaller caller, String[] args) {
        if (!SettingsManager.isEnabled(ForceBattleSettings.BACKPACK)) {
            caller.sendMessage(Component.text("Backpacks are disabled.").color(KatzeColor.RED));
            return;
        }
        if (ForceBattleGameListener.startPhase) {
            caller.sendMessage(Component.text("The game hasn't started yet.").color(KatzeColor.RED));
            return;
        }

        Optional<KatzePlayer> senderOpt = KatzePlayer.adapt(caller.getName());
        senderOpt.filter(ForceBattlePlayer.class::isInstance)
                .map(ForceBattlePlayer.class::cast)
                .ifPresent(ForceBattlePlayer::openBackpack);
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        return List.of();
    }
}

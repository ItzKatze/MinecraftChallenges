package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.game.GameLifecycleManager;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResetCommand extends KatzeCommand {

    public ResetCommand() {
        super(
                "reset",
                List.of(),
                CallerType.NONE,
                "/reset",
                PermissionDefault.OP,
                "Command to reset the configured game"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (ChallengeManager.getActiveChallenges().isEmpty()) {
            caller.sendMessage(Component.text("No challenge is enabled, nothing to reset.").color(KatzeColor.RED));
            return;
        }

        GameLifecycleManager.resetGame();
        caller.sendMessage(Component.text("The game has been reset.").color(KatzeColor.LIME));
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }
}

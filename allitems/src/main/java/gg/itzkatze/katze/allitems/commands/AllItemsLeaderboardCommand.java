package gg.itzkatze.katze.allitems.commands;

import gg.itzkatze.katze.allitems.gui.AllItemsLeaderboardGUI;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AllItemsLeaderboardCommand extends KatzeCommand {

    public AllItemsLeaderboardCommand() {
        super(
                "leaderboard",
                List.of(),
                CallerType.PLAYER,
                "/leaderboard",
                PermissionDefault.TRUE,
                "Open the All Items leaderboard"
        );
    }

    @Override
    protected void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (caller instanceof KatzePlayer player) {
            new AllItemsLeaderboardGUI().open(player);
        } else {
            caller.sendMessage(Component.text("This command can only be used by players."));
        }
    }

    @Override
    protected List<String> tabComplete(@NotNull CommandCaller caller, String[] args) {
        return List.of();
    }
}

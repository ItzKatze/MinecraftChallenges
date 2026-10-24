package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.player.KatzePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MobileCraftingCommand extends KatzeCommand {

    public MobileCraftingCommand() {
        super(
                "craft",
                List.of("crafting"),
                CallerType.PLAYER,
                "/craft",
                PermissionDefault.TRUE,
                "Command to open the crafting table GUI"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (!SettingsManager.isEnabled(GlobalSetting.MOBILE_CRAFTING)) {
            caller.sendMessage(Component.text("Mobile Crafting is disabled.").color(NamedTextColor.RED));
            return;
        }
        if (caller instanceof KatzePlayer katzePlayer) {
            Player player = katzePlayer.getPlayer();
            player.openWorkbench(null, true);
        }
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        return List.of();
    }
}

package gg.itzkatze.mcc.commands;

import gg.itzkatze.mcc.commands.framework.CallerType;
import gg.itzkatze.mcc.commands.framework.CommandCaller;
import gg.itzkatze.mcc.commands.framework.MccCommand;
import gg.itzkatze.mcc.lobby.cosmetics.Rarity;
import gg.itzkatze.mcc.lobby.cosmetics.cases.CaseFireworks;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CaseFireworksCommand extends MccCommand {

    public CaseFireworksCommand() {
        super(
                "casefireworks",
                List.of(),
                CallerType.PLAYER,
                "/casefireworks [rarity]",
                PermissionDefault.OP,
                "Plays the firework show of a Cosmetic Case prize"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (!(caller instanceof MccPlayer mccPlayer) || mccPlayer.getPlayer() == null) return;

        Rarity tier = args.length == 0 ? Rarity.MYTHIC : rarity(args[0]);
        if (tier == null) {
            caller.sendMessage(Component.text("Unknown rarity \"" + args[0] + "\". Rarities: " + String.join(", ", rarities()) + ".", NamedTextColor.RED));
            return;
        }
        CaseFireworks.play(mccPlayer.getPlayer(), tier);
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        if (args.length != 1) return List.of();
        return StringUtility.copyPartialMatches(args[0], rarities(), new ArrayList<>());
    }

    private static @Nullable Rarity rarity(@NotNull String name) {
        return Arrays.stream(Rarity.values()).filter(tier -> tier.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private static @NotNull List<String> rarities() {
        return Arrays.stream(Rarity.values()).map(tier -> tier.name().toLowerCase(Locale.ROOT)).toList();
    }
}

package gg.itzkatze.katze.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.lobby.cosmetics.Rarity;
import gg.itzkatze.katze.lobby.cosmetics.cases.CaseFireworks;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CaseFireworksCommand extends KatzeCommand {

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
        if (!(caller instanceof KatzePlayer katzePlayer) || katzePlayer.getPlayer() == null) return;

        Rarity tier = args.length == 0 ? Rarity.MYTHIC : rarity(args[0]);
        if (tier == null) {
            caller.sendMessage(Component.text("Unknown rarity \"" + args[0] + "\". Rarities: " + String.join(", ", rarities()) + ".", KatzeColor.RED));
            return;
        }
        CaseFireworks.play(katzePlayer.getPlayer(), tier);
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

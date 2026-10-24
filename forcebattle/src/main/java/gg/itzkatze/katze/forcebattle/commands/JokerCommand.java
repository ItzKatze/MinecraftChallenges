package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.forcebattle.ForceBattlePlayer;
import gg.itzkatze.katze.commands.framework.CallerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.TabCompletions;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JokerCommand extends KatzeCommand {

    public JokerCommand() {
        super(
                "joker",
                List.of(),
                CallerType.NONE,
                "/joker <add|remove> <player> <jokertype> <amount>",
                PermissionDefault.OP,
                "Command to add or remove jokers from a player"
        );
    }

    @Override
    protected void executeCommand(final CommandCaller caller, final String[] args) {
        if (args.length < 3) {
            sendUsage(caller);
            return;
        }

        Optional<ForceBattlePlayer> targetOptional = ForceBattlePlayer.from(args[1]);
        if (targetOptional.isEmpty()) {
            caller.sendMessage(Component.text("Player couldn't be found or is not in Force Battle.").color(NamedTextColor.RED));
            return;
        }
        ForceBattlePlayer target = targetOptional.get();

        JokerType jokerType = JokerType.of(args[2]);
        if (jokerType == null) {
            caller.sendMessage(Component.text("Invalid joker type. Available types: 'skip', 'swap'.").color(NamedTextColor.RED));
            return;
        }

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                caller.sendMessage(Component.text("Please specify a valid number.").color(NamedTextColor.RED));
                return;
            }
        }

        switch (args[0]) {
            case "add" -> {
                switch (jokerType) {
                    case PLAYERSKIP -> target.addPlayerSkip(amount);
                    case TEAMSKIP -> target.addTeamSkip(amount);
                    case SWAP -> target.addSwap(amount);
                }
            }
            case "remove" -> {
                switch (jokerType) {
                    case PLAYERSKIP -> target.removePlayerSkip(amount);
                    case TEAMSKIP -> target.removeTeamSkip(amount);
                    case SWAP -> target.removeSwap(amount);
                }
            }
        }
    }

    @Override
    protected List<String> tabComplete(final CommandCaller caller, final String[] args) {
        if (args.length == 1) {
            return StringUtility.copyPartialMatches(args[0], List.of("add", "remove"), new ArrayList<>());
        }
        if (args.length == 2) {
            return StringUtility.copyPartialMatches(args[1], TabCompletions.getPlayerNamesTabCompletions(), new ArrayList<>());
        }
        if (args.length == 3) {
            return StringUtility.copyPartialMatches(args[2], List.of(JokerType.PLAYERSKIP.getIdentifier(), JokerType.SWAP.getIdentifier(),
                    JokerType.TEAMSKIP.getIdentifier()), new ArrayList<>());
        }
        if (args.length == 4) {
            return StringUtility.copyPartialMatches(args[3], List.of("amount"), new ArrayList<>());
        }
        return List.of();
    }

    private enum JokerType {
        PLAYERSKIP,
        TEAMSKIP,
        SWAP;

        private final String IDENTIFIER = name().toLowerCase();

        public String getIdentifier() {
            return IDENTIFIER;
        }

        public static JokerType of(final String identifier) {
            for (JokerType jokerType : values()) {
                if (jokerType.getIdentifier().equals(identifier)) {
                    return jokerType;
                }
            }
            return null;
        }
    }

}

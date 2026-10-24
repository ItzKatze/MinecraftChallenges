package gg.itzkatze.katze.commands.framework;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class KatzeCommand {

    public static final List<KatzeCommand> COMMANDS = new ArrayList<>();
    public final String id;
    public final List<String> aliases;
    public final CallerType requiredType;
    public final String usage;
    public final PermissionDefault permission;
    public final String description;

    private static final Logger logger = LoggerFactory.getLogger("ForceBattle/" + KatzeCommand.class.getSimpleName());

    public KatzeCommand(
            String id, List<String> aliases, CallerType requiredType,
            String usage, PermissionDefault permission, final String description
    ) {
        this.id = id;
        this.aliases = aliases;
        this.requiredType = requiredType;
        this.usage = usage;
        this.permission = permission;
        this.description = description;

        COMMANDS.add(this);
    }

    public static void unregister(@NotNull KatzeCommand command) {
        COMMANDS.remove(command);
    }

    public static @NotNull Set<String> getRegisteredCommandNames() {
        return COMMANDS.stream()
                .flatMap(c -> Stream.concat(Stream.of(c.id), c.aliases.stream()))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public static @NotNull Optional<KatzeCommand> getCommandById(String commandId) {
        for (KatzeCommand command : COMMANDS) {
            if (command.matches(commandId)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public static void execute(String commandId, CommandCaller caller, String[] args) {
        @NotNull Optional<KatzeCommand> commandOptional = getCommandById(commandId);
        if (commandOptional.isPresent()) {
            commandOptional.get().execute(caller, args);
        } else {
            logger.error("Error while trying to execute command: {}. Command not registered.", commandId);
            caller.sendMessage(Component.text("This command is not available right now."));
        }
    }

    public static @NotNull @Unmodifiable List<String> tabComplete(String commandId, CommandCaller caller, String[] args) {
        @NotNull Optional<KatzeCommand> commandOptional = getCommandById(commandId);
        if (commandOptional.isPresent()) {
            return commandOptional.get().getTabCompletions(caller, args);
        }
        logger.error("Error while trying to get tab-completions for command: {}. Command not registered.", commandId);
        return Collections.emptyList();
    }

    public boolean cannotExecute(CommandCaller caller, boolean message) {
        if (caller == null) {
            return false;
        }
        if (!this.requiredType.allows(caller)) {
            if (message) {
                caller.sendMessage(Component.text("This command can only be used by " + this.requiredType.name().toLowerCase() + "s."));
            }
        } else if (!caller.hasPermission(getPermission())) {
            if (message) {
                caller.sendMessage(Component.text("Insufficient permission to execute this command!"));
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean matches(String commandId) {
        return this.id.equalsIgnoreCase(commandId) || this.aliases.contains(commandId.toLowerCase());
    }

    public void sendUsage(@NotNull CommandCaller caller) {
        caller.sendMessage(Component.text("Usage: " + this.usage));
    }

    protected void execute(CommandCaller caller, String @NotNull [] args) {
        if (cannotExecute(caller, true)) {
            return;
        }
        executeCommand(caller, args);
    }

    protected List<String> getTabCompletions(CommandCaller caller, String[] args) {
        if (cannotExecute(caller, false)) {
            return List.of();
        }
        return tabComplete(caller, args);
    }

    protected abstract void executeCommand(CommandCaller caller, String[] args);

    protected abstract List<String> tabComplete(CommandCaller caller, String[] args);

}

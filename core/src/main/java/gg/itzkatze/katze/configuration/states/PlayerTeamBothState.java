package gg.itzkatze.katze.configuration.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public enum PlayerTeamBothState implements SettingStateType {
    OFF(Component.text("Off").color(NamedTextColor.RED)),
    PLAYER(Component.text("Player").color(NamedTextColor.GREEN)),
    TEAM(Component.text("Team").color(NamedTextColor.AQUA)),
    BOTH(Component.text("Both").color(NamedTextColor.GOLD));

    private final Component defaultName;

    PlayerTeamBothState(Component defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    @NotNull public PlayerTeamBothState next() {
        return switch (this) {
            case OFF -> PLAYER;
            case PLAYER -> TEAM;
            case TEAM -> BOTH;
            case BOTH -> OFF;
        };
    }

    @Override
    @NotNull public Component getDisplayName() {
        return defaultName;
    }
}

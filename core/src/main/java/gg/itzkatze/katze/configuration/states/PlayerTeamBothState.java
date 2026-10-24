package gg.itzkatze.katze.configuration.states;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum PlayerTeamBothState implements SettingStateType {
    OFF(Component.text("Off").color(KatzeColor.RED)),
    PLAYER(Component.text("Player").color(KatzeColor.LIME)),
    TEAM(Component.text("Team").color(KatzeColor.LIGHT_BLUE)),
    BOTH(Component.text("Both").color(KatzeColor.GOLD));

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

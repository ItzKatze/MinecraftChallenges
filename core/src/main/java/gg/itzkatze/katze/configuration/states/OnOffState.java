package gg.itzkatze.katze.configuration.states;

import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public enum OnOffState implements SettingStateType {
    OFF(Component.text("Off").color(KatzeColor.RED)),
    ON(Component.text("On").color(KatzeColor.LIME));

    private final Component displayName;

    OnOffState(Component displayName) {
        this.displayName = displayName;
    }

    @Override
    @NonNull public OnOffState next() {
        return this == OFF ? ON : OFF;
    }

    @Override
    @NotNull public Component getDisplayName() {
        return displayName;
    }
}

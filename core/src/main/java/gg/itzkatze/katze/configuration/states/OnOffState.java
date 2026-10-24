package gg.itzkatze.katze.configuration.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public enum OnOffState implements SettingStateType {
    OFF(Component.text("Off").color(NamedTextColor.RED)),
    ON(Component.text("On").color(NamedTextColor.GREEN));

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

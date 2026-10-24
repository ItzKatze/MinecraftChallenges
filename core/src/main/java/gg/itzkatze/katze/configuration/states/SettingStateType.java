package gg.itzkatze.katze.configuration.states;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface SettingStateType {

    @NotNull Component getDisplayName();

    @NotNull SettingStateType next();
}

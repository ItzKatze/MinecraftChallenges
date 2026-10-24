package gg.itzkatze.katze.configuration.states;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConfigurableSettingState extends SettingStateType {

    @NotNull String serialize();

    @Nullable SettingStateType parse(@NotNull String raw);
}

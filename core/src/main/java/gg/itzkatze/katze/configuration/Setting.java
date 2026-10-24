package gg.itzkatze.katze.configuration;

import gg.itzkatze.katze.configuration.states.SettingStateType;

public interface Setting {

    String getId();

    String getDisplayName();

    boolean shouldUpdateOnChange();

    SettingStateType getDefaultState();
}

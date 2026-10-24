package gg.itzkatze.katze.configuration;

import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum GlobalSetting implements Setting {
    NO_HUNGER(false, "No Hunger", OnOffState.OFF),
    NO_DAMAGE(false, "No Damage", OnOffState.OFF),
    MOBILE_CRAFTING(false, "Mobile Crafting", OnOffState.OFF),
    HIDE_POINTS(false, "Hide Points", OnOffState.ON),
    HIDE_OBJECTIVES(false, "Hide Objectives", OnOffState.OFF),
    NO_DUPLICATE_OBJECTIVES(true, "No Duplicate Objectives", OnOffState.ON),
    SIMPLIFIED_OBJECTIVES(true, "Simplified Objectives", OnOffState.ON),
    EXTRA_TEAM_OBJECTIVE(true, "Extra Team Objective", OnOffState.ON),
    SHOW_SCOREBOARD(false, "Show Scoreboard", OnOffState.ON);

    private final boolean shouldUpdateOnChange;
    private final String displayName;
    private final SettingStateType defaultState;

    GlobalSetting(boolean shouldUpdateOnChange, String displayName, SettingStateType defaultState) {
        this.shouldUpdateOnChange = shouldUpdateOnChange;
        this.displayName = displayName;
        this.defaultState = defaultState;
    }

    @Override
    @NotNull public String getId() {
        return name();
    }

    @Override
    public boolean shouldUpdateOnChange() {
        return shouldUpdateOnChange;
    }

    @Override
    public SettingStateType getDefaultState() {
        return defaultState;
    }
}

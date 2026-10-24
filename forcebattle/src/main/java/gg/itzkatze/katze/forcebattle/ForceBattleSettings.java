package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import lombok.Getter;

@Getter
public enum ForceBattleSettings implements Setting {
    FORCE_ITEM(true, "Item", PlayerTeamBothState.BOTH),
    FORCE_MOB(true, "Mob", PlayerTeamBothState.TEAM),
    FORCE_BIOME(true, "Biome", PlayerTeamBothState.TEAM),
    FORCE_ADVANCEMENT(true, "Advancement", PlayerTeamBothState.TEAM),
    FORCE_HEIGHT(true, "Height", PlayerTeamBothState.TEAM),
    FORCE_COORDS(true, "Coords", PlayerTeamBothState.TEAM),
    FORCE_STRUCTURE(true, "Structure", PlayerTeamBothState.TEAM),
    FORCE_DEATH(true, "Death", PlayerTeamBothState.TEAM),

    BACKPACK(false, "Backpack", OnOffState.ON),
    CHAIN_MODE(true, "Chain Mode", OnOffState.OFF),

    EXCLUDE_MUSIC_DISCS(true, "Exclude Music Discs", OnOffState.ON),
    EXCLUDE_BANNER_PATTERNS(true, "Exclude Banner Patterns", OnOffState.ON),
    EXCLUDE_ARMOR_TEMPLATES(true, "Exclude Armor Templates", OnOffState.ON),
    EXCLUDE_POTTERY_SHERDS(true, "Exclude Pottery Sherds", OnOffState.ON),
    EXCLUDE_ORES(true, "Exclude Ores", OnOffState.ON),
    EXCLUDE_END(true, "Exclude End", OnOffState.ON),
    EXCLUDE_TRIAL_ITEMS(true, "Exclude Trial Items", OnOffState.ON),
    EXCLUDE_OXIDIZED_COPPER(true, "Exclude Oxidized Copper", OnOffState.ON),
    EXCLUDE_HEADS(true, "Exclude Heads", OnOffState.ON),
    EXCLUDE_SNIFFER(true, "Exclude Sniffer", OnOffState.ON),
    EXCLUDE_UNREALISTIC(true, "Exclude Unrealistic", OnOffState.ON),
    EXCLUDE_MAPS(true, "Exclude Maps", OnOffState.ON);

    private final boolean updateOnChange;
    private final String displayName;
    private final SettingStateType defaultState;

    ForceBattleSettings(boolean updateOnChange, String displayName, SettingStateType defaultState) {
        this.updateOnChange = updateOnChange;
        this.displayName = displayName;
        this.defaultState = defaultState;
    }

    @Override
    public String getId() {
        return name();
    }

    @Override
    public boolean shouldUpdateOnChange() {
        return updateOnChange;
    }

    @Override
    public SettingStateType getDefaultState() {
        return defaultState;
    }
}

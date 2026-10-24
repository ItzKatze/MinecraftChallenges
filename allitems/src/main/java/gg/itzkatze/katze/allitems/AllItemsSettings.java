package gg.itzkatze.katze.allitems;

import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import lombok.Getter;
import lombok.Setter;

public class AllItemsSettings {

    public enum GameMode {
        TEAM,
        INDIVIDUAL
    }

    @Setter
    @Getter
    private static GameMode gameMode = GameMode.TEAM;

    public static boolean isTeamMode() {
        return gameMode == GameMode.TEAM;
    }

    public static boolean isIndividualMode() {
        return gameMode == GameMode.INDIVIDUAL;
    }

    public static boolean isItemsEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.ITEMS);
    }

    public static boolean isBiomesEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.BIOMES);
    }

    public static boolean isMobsEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.MOBS);
    }

    public static boolean isStructuresEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.STRUCTURES);
    }

    public static boolean isAdvancementsEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.ADVANCEMENTS);
    }

    public static boolean isChainModeEnabled() {
        return SettingsManager.isEnabled(AllItemsSetting.CHAIN_MODE);
    }

    @Getter
    public enum AllItemsSetting implements Setting {
        ITEMS(false, "Items", OnOffState.ON),
        BIOMES(false, "Biomes", OnOffState.OFF),
        MOBS(false, "Mobs", OnOffState.OFF),
        STRUCTURES(false, "Structures", OnOffState.OFF),
        ADVANCEMENTS(false, "Advancements", OnOffState.OFF),
        CHAIN_MODE(false, "Chain Mode", OnOffState.OFF);

        private final boolean updateOnChange;
        private final String displayName;
        private final SettingStateType defaultState;

        AllItemsSetting(boolean updateOnChange, String displayName, SettingStateType defaultState) {
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
}

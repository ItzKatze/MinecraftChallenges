package gg.itzkatze.katze.randomizer;

import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import lombok.Getter;

public class RandomizerSettings {

    public static boolean isBlockDropsEnabled() {
        return SettingsManager.isEnabled(RandomizerSetting.BLOCK_DROPS);
    }

    public static boolean isMobDropsEnabled() {
        return SettingsManager.isEnabled(RandomizerSetting.MOB_DROPS);
    }

    public static boolean isChestLootEnabled() {
        return SettingsManager.isEnabled(RandomizerSetting.CHEST_LOOT);
    }

    public static boolean isSharedMappingsEnabled() {
        return SettingsManager.isEnabled(RandomizerSetting.SHARED_MAPPINGS);
    }

    @Getter
    public enum RandomizerSetting implements Setting {
        BLOCK_DROPS(false, "Block Drops", OnOffState.ON),
        MOB_DROPS(false, "Mob Drops", OnOffState.OFF),
        CHEST_LOOT(false, "Chest Loot", OnOffState.ON),
        SHARED_MAPPINGS(true, "Shared Mappings", OnOffState.ON);

        private final boolean updateOnChange;
        private final String displayName;
        private final SettingStateType defaultState;

        RandomizerSetting(boolean updateOnChange, String displayName, SettingStateType defaultState) {
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

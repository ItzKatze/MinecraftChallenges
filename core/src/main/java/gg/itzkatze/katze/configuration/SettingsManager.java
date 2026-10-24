package gg.itzkatze.katze.configuration;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.player.KatzePlayer;

public class SettingsManager {

    private static final ChallengeSettingsManager GLOBAL_SETTINGS_MANAGER =
            new ChallengeSettingsManager(null, GlobalSetting.class, SettingsManager::onGlobalSettingChanged);

    private SettingsManager() {}

    private static void onGlobalSettingChanged(Challenge challenge, Setting setting, SettingStateType oldState, SettingStateType newState) {
    }

    public static ChallengeSettingsManager getGlobalSettingsManager() {
        return GLOBAL_SETTINGS_MANAGER;
    }

    public static SettingStateType getState(Setting setting) {
        return GLOBAL_SETTINGS_MANAGER.getState(setting);
    }

    public static void setState(Setting setting, SettingStateType state) {
        if (state == null) {
            state = setting.getDefaultState();
        }
        ChallengeSettingsManager manager = findSettingsManagerFor(setting);
        if (state != null && manager != null) {
            manager.setState(setting, state);
        }
    }

    public static boolean isEnabled(Setting setting) {
        if (setting instanceof GlobalSetting) {
            return GLOBAL_SETTINGS_MANAGER.isEnabled(setting);
        }
        ChallengeSettingsManager manager = findSettingsManagerFor(setting);
        if (manager != null) {
            return manager.isEnabled(setting);
        }
        return false;
    }

    private static void onGlobalSettingChanged(Object challenge, Setting setting, PlayerTeamBothState oldState, PlayerTeamBothState newState) {
        if (!(setting instanceof GlobalSetting)) {
            return;
        }

        KatzePlayer.PLAYERS.forEach(player -> player.updateObjective(false, false));
        KatzeTeam.teams.forEach(team -> team.updateObjective(null, false, false));
    }

    private static ChallengeSettingsManager findSettingsManagerFor(Object setting) {
        if (setting instanceof GlobalSetting) {
            return GLOBAL_SETTINGS_MANAGER;
        }

        for (Challenge challenge : ChallengeRegistry.getAllChallenges()) {
            ChallengeSettingsManager challengeManager = challenge.getSettingsManager();
            if (challengeManager == null) continue;

            try {
                for (Setting enumValue : challengeManager.getAllSettings()) {
                    if (enumValue == setting || enumValue.equals(setting)) {
                        return challengeManager;
                    }
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean isEnabled(Object setting) {
        if (setting instanceof GlobalSetting globalSetting) {
            return GLOBAL_SETTINGS_MANAGER.isEnabled(globalSetting);
        }

        ChallengeSettingsManager manager = findSettingsManagerFor(setting);
        if (manager != null && setting instanceof Setting s) {
            try {
                return manager.isEnabled((Enum<?> & Setting) setting);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static SettingStateType getSettingState(Object setting) {
        if (setting instanceof GlobalSetting globalSetting) {
            return GLOBAL_SETTINGS_MANAGER.getState(globalSetting);
        }

        ChallengeSettingsManager manager = findSettingsManagerFor(setting);
        if (manager != null && setting instanceof Setting s) {
            try {
                return manager.getState((Enum<?> & Setting) setting);
            } catch (Exception e) {
            }
        }
        return null;
    }
}

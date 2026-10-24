package gg.itzkatze.katze.challenges;

import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.gui.KatzeGUI;
import org.jetbrains.annotations.Nullable;

public interface ChallengeSettings {

    default KatzeGUI getSettingsGUI() {
        return null;
    }

    default boolean hasSettings() {
        return getSettingsGUI() != null || getSettingsManager() != null;
    }

    @Nullable default ChallengeSettingsManager getSettingsManager() {
        return null;
    }
}

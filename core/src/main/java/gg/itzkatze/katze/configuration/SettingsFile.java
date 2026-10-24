package gg.itzkatze.katze.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SettingsFile {

    private static final Logger logger = LoggerFactory.getLogger("ForceBattle/" + SettingsFile.class.getSimpleName());
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static void loadSettingsFromFile(File jsonFile) throws IOException {
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonObject settingsObject = JsonParser.parseReader(reader).getAsJsonObject();

            for (Challenge challenge : ChallengeRegistry.getAllChallenges()) {
                loadChallengeSettings(challenge, settingsObject);
            }
        }
    }

    private static void loadChallengeSettings(Challenge challenge, JsonObject settingsObject) {
        ChallengeSettingsManager settingsManager = challenge.getSettingsManager();
        String challengeKey = challenge.getType().name().toLowerCase();
        if (settingsManager == null) return;

        try {
            for (Setting setting : settingsManager.getAllSettings()) {
                JsonElement value = settingsObject.get(setting.getId());
                if (value == null) {
                    continue;
                }
                String stateStr = value.getAsString();
                if (!settingsManager.importState(setting, stateStr)) {
                    logger.warn(
                            "Invalid state '{}' for setting {} in {}, using default state",
                            stateStr, setting.getId(), challengeKey
                    );
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load settings for challenge {}: {}", challengeKey, e.getMessage());
        }
    }

    public static void saveToFile(File jsonFile) throws IOException {
        JsonObject settingsObject = new JsonObject();

        for (Challenge challenge : ChallengeRegistry.getAllChallenges()) {
            saveChallengeSettings(challenge, settingsObject);
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            GSON.toJson(settingsObject, writer);
        }
    }

    private static void saveChallengeSettings(Challenge challenge, JsonObject settingsObject) {
        ChallengeSettingsManager settingsManager = challenge.getSettingsManager();
        String challengeKey = challenge.getType().name().toLowerCase();
        if (settingsManager == null) return;

        try {
            settingsManager.exportStates().forEach(settingsObject::addProperty);
        } catch (Exception e) {
            logger.warn("Failed to save settings for challenge {}: {}", challengeKey, e.getMessage());
        }
    }

}

package gg.itzkatze.mcc;

import gg.itzkatze.mcc.bossbar.BossbarManager;
import gg.itzkatze.mcc.challenges.ChallengeManager;
import gg.itzkatze.mcc.chat.LevelChat;
import gg.itzkatze.mcc.chat.TeamChatManager;
import gg.itzkatze.mcc.commands.framework.MccCommand;
import gg.itzkatze.mcc.commands.framework.CommandHandler;
import gg.itzkatze.mcc.commands.framework.CommandVisibilityListener;
import gg.itzkatze.mcc.game.GameLifecycleManager;
import gg.itzkatze.mcc.game.MccTimer;
import gg.itzkatze.mcc.game.PauseManager;
import gg.itzkatze.mcc.game.SessionPersistence;
import gg.itzkatze.mcc.gui.GUIListener;
import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.level.LevelUpNotice;
import gg.itzkatze.mcc.level.PlaytimeXp;
import gg.itzkatze.mcc.level.XpDigest;
import gg.itzkatze.mcc.player.AfkTracker;
import gg.itzkatze.mcc.lobby.LobbyManager;
import gg.itzkatze.mcc.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.mcc.lobby.cosmetics.cases.CaseFireworkGuard;
import gg.itzkatze.mcc.lobby.cosmetics.cases.CaseRollGui;
import gg.itzkatze.mcc.lobby.cosmetics.PointsSidebar;
import gg.itzkatze.mcc.player.data.PlayerDataStore;
import gg.itzkatze.mcc.gui.sign.SignInput;
import gg.itzkatze.mcc.configuration.SettingsFile;
import gg.itzkatze.mcc.game.GameTimer;
import gg.itzkatze.mcc.game.TimerRegistry;
import gg.itzkatze.mcc.game.TaskManager;
import gg.itzkatze.mcc.scoreboard.NameTags;
import gg.itzkatze.mcc.scoreboard.ScoreboardManager;
import gg.itzkatze.mcc.spectator.SpectatorChat;
import gg.itzkatze.mcc.spectator.SpectatorListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Stream;

public final class MinecraftChallenges extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger("MinecraftChallenges");
    private static MinecraftChallenges instance;
    private final PauseManager pauseManager = new PauseManager();

    public static MinecraftChallenges get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        File settingsFile = new File(getDataDirectory(), "data/settings.json");
        if (!settingsFile.exists()) {
            File parent = settingsFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            saveResource("data/settings.json", false);
        }

        try {
            SettingsFile.loadSettingsFromFile(settingsFile);
        } catch (IOException e) {
            throw new RuntimeException("settings.json File not found", e);
        }
        installTimerEngine();
        PlayerDataStore.load();
        CosmeticStore.load();
        LevelStore.load();
        PointsSidebar.install(CosmeticStore.get(), LevelStore.get());
        LevelUpNotice.install(LevelStore.get());

        CommandHandler.registerAll(loopThroughPackage("gg.itzkatze.mcc.commands", MccCommand.class).toList());
        getServer().getPluginManager().registerEvents(new CommandVisibilityListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new SignInput(), this);
        BossbarManager.runTask();
        ScoreboardManager.startUpdater();
        NameTags.start();
        XpDigest.start();

        getServer().getPluginManager().registerEvents(new GameLifecycleManager(), this);
        getServer().getPluginManager().registerEvents(new LobbyManager(), this);
        getServer().getPluginManager().registerEvents(new CaseFireworkGuard(), this);
        LobbyManager.loadIslands();
        getServer().getPluginManager().registerEvents(new TeamChatManager(), this);
        getServer().getPluginManager().registerEvents(new LevelChat(), this);
        SpectatorListener spectatorListener = new SpectatorListener();
        getServer().getPluginManager().registerEvents(spectatorListener, this);
        getServer().getPluginManager().registerEvents(new SpectatorChat(), this);
        spectatorListener.startViewRefresh();
        getServer().getPluginManager().registerEvents(pauseManager, this);
        pauseManager.startStateWatcher();
        AfkTracker afk = new AfkTracker();
        getServer().getPluginManager().registerEvents(afk, this);
        new PlaytimeXp(afk).start();

        SessionPersistence.restore();

        logger.info("Successfully initialized MinecraftChallenges.");
    }

    @Override
    public void onDisable() {
        CaseRollGui.finishAllSilently();
        PlayerDataStore.flushInstalled();
        BossbarManager.stopTask();
        SessionPersistence.saveNow();
        ChallengeManager.unloadAllChallenges();
        TaskManager.stopAll();
        try {
            SettingsFile.saveToFile(new File(getDataDirectory(), "data/settings.json"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings to file", e);
        }
        getConfig().set("first-startup", false);
        saveConfig();
    }

    public @NotNull File getDataDirectory() {
        return getDataFolder();
    }

    public int getGameDuration() {
        return getConfig().getInt("game-duration", 5400);
    }

    public static GameTimer getTimer() {
        return TimerRegistry.getTimer();
    }

    private void installTimerEngine() {
        TimerRegistry.setTimer(new MccTimer(getGameDuration(), false));
    }

    public static void broadcast(Component message) {
        Bukkit.getServer().sendMessage(message);
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);

        return subTypes.stream()
                .map(subClass -> {
                    try {
                        return clazz.cast(subClass.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull);
    }
}

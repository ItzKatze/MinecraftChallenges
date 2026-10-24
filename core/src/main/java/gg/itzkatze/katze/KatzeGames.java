package gg.itzkatze.katze;

import gg.itzkatze.katze.bossbar.BossbarManager;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.chat.LevelChat;
import gg.itzkatze.katze.chat.TeamChatManager;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandHandler;
import gg.itzkatze.katze.commands.framework.CommandVisibilityListener;
import gg.itzkatze.katze.game.GameLifecycleManager;
import gg.itzkatze.katze.game.KatzeTimer;
import gg.itzkatze.katze.game.PauseManager;
import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.gui.GUIListener;
import gg.itzkatze.katze.level.LevelStore;
import gg.itzkatze.katze.level.LevelUpNotice;
import gg.itzkatze.katze.level.PlaytimeXp;
import gg.itzkatze.katze.level.XpDigest;
import gg.itzkatze.katze.player.AfkTracker;
import gg.itzkatze.katze.lobby.LobbyManager;
import gg.itzkatze.katze.lobby.cosmetics.ClientModHint;
import gg.itzkatze.katze.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.katze.lobby.cosmetics.MusicChannel;
import gg.itzkatze.katze.lobby.cosmetics.MusicManager;
import gg.itzkatze.katze.lobby.cosmetics.cases.CaseFireworkGuard;
import gg.itzkatze.katze.lobby.cosmetics.cases.CaseRollGui;
import gg.itzkatze.katze.lobby.cosmetics.PointsSidebar;
import gg.itzkatze.katze.player.data.PlayerDataStore;
import gg.itzkatze.katze.gui.sign.SignInput;
import gg.itzkatze.katze.configuration.SettingsFile;
import gg.itzkatze.katze.game.GameTimer;
import gg.itzkatze.katze.game.TimerRegistry;
import gg.itzkatze.katze.game.TaskManager;
import gg.itzkatze.katze.scoreboard.NameTags;
import gg.itzkatze.katze.scoreboard.ScoreboardManager;
import gg.itzkatze.katze.spectator.SpectatorChat;
import gg.itzkatze.katze.spectator.SpectatorListener;
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

public final class KatzeGames extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger("KatzeGames");
    private static KatzeGames instance;
    private final PauseManager pauseManager = new PauseManager();

    public static KatzeGames get() {
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
        MusicChannel.register(this);
        LevelStore.load();
        PointsSidebar.install(CosmeticStore.get(), LevelStore.get());
        LevelUpNotice.install(LevelStore.get());

        CommandHandler.registerAll(loopThroughPackage("gg.itzkatze.katze.commands", KatzeCommand.class).toList());
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
        getServer().getPluginManager().registerEvents(new ClientModHint(), this);
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

        logger.info("Successfully initialized KatzeGames.");
    }

    @Override
    public void onDisable() {
        MusicManager.releaseAll();
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
        TimerRegistry.setTimer(new KatzeTimer(getGameDuration(), false));
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

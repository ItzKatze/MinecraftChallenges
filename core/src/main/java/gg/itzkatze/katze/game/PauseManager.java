package gg.itzkatze.katze.game;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.ChallengeManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PauseManager implements Listener {

    private static final NamespacedKey PAUSED_AI_KEY = new NamespacedKey("katzegames", "paused_ai");

    private boolean pauseApplied = false;
    private final Map<String, Integer> randomTickSpeedByWorld = new HashMap<>();
    private final Map<UUID, Location> frozenLocations = new HashMap<>();
    private final Set<UUID> aiDisabledEntities = new HashSet<>();

    public void startStateWatcher() {
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(this::restoreEntityAI));
        Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), this::updatePauseState, 1L, 5L);
    }

    private void updatePauseState() {
        boolean shouldPause = ChallengeManager.isGameStarted() &&
                KatzeGames.getTimer().getMode() != GameTimer.Mode.DISABLED &&
                !KatzeGames.getTimer().hasHitZero() &&
                !KatzeGames.getTimer().isRunning();
        if (shouldPause && !pauseApplied) {
            applyPause();
        } else if (!shouldPause && pauseApplied) {
            clearPause();
        }
    }

    private void applyPause() {
        pauseApplied = true;
        randomTickSpeedByWorld.clear();

        for (World world : Bukkit.getWorlds()) {
            Integer randomTickSpeed = world.getGameRuleValue(GameRules.RANDOM_TICK_SPEED);
            // A speed of 0 is most likely left over from a pause the server crashed in, so resuming falls back to the default.
            if (randomTickSpeed != null && randomTickSpeed != 0) {
                randomTickSpeedByWorld.put(world.getName(), randomTickSpeed);
            }

            world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
            world.setGameRule(GameRules.ADVANCE_TIME, false);
            world.setGameRule(GameRules.ADVANCE_WEATHER, false);

            for (Entity entity : world.getEntities()) {
                entity.setVelocity(new Vector(0, 0, 0));
                if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
                    try {
                        if (livingEntity.hasAI()) {
                            livingEntity.setAI(false);
                            livingEntity.getPersistentDataContainer().set(PAUSED_AI_KEY, PersistentDataType.BOOLEAN, true);
                            aiDisabledEntities.add(livingEntity.getUniqueId());
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    private void clearPause() {
        pauseApplied = false;

        for (World world : Bukkit.getWorlds()) {
            Integer randomTickSpeed = randomTickSpeedByWorld.get(world.getName());

            world.setGameRule(GameRules.RANDOM_TICK_SPEED, randomTickSpeed == null ? 20 : randomTickSpeed);
            world.setGameRule(GameRules.ADVANCE_TIME, true);
            world.setGameRule(GameRules.ADVANCE_WEATHER, true);

            for (Entity entity : world.getEntities()) {
                restoreEntityAI(entity);
            }
        }

        frozenLocations.clear();
    }

    private void restoreEntityAI(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        UUID entityId = livingEntity.getUniqueId();
        boolean pausedByPlugin = aiDisabledEntities.contains(entityId) || livingEntity.getPersistentDataContainer().has(PAUSED_AI_KEY);
        if (!pausedByPlugin) {
            return;
        }
        try {
            livingEntity.setAI(true);
            livingEntity.getPersistentDataContainer().remove(PAUSED_AI_KEY);
            aiDisabledEntities.remove(entityId);
        } catch (Throwable ignored) {
        }
    }

    private boolean isPausedGameplay() {
        return pauseApplied && ChallengeManager.isGameStarted();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isPausedGameplay()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        boolean movedPosition = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        if (!movedPosition) return;

        UUID playerId = event.getPlayer().getUniqueId();
        Location freezeAt = frozenLocations.computeIfAbsent(playerId, id -> from.clone());
        event.setTo(freezeAt);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isPausedGameplay()) return;
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockCook(BlockCookEvent event) {
        if (isPausedGameplay()) event.setCancelled(true);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (pauseApplied) {
            return;
        }
        for (Entity entity : event.getEntities()) {
            restoreEntityAI(entity);
        }
    }
}

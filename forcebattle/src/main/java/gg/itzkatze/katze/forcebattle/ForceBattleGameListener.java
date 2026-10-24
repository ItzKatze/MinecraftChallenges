package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.forcebattle.tasks.*;
import gg.itzkatze.katze.util.Joker;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.team.KatzeTeam;
import org.bukkit.event.Listener;
import gg.itzkatze.katze.event.PlayerTeamJoinEvent;
import gg.itzkatze.katze.event.PlayerTeamLeaveEvent;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ForceBattleGameListener implements Listener {

    public static boolean startPhase = true;
    private final Map<UUID, List<ItemStack>> pendingItemRestores = new HashMap<>();
    private final ForceBattleWalking walking = new ForceBattleWalking();

    public void clearPendingRestores() {
        pendingItemRestores.clear();
    }

    public void resetWalking() {
        walking.reset();
    }

    public ForceBattleGameListener() {

    }

    public void runTasks() {
        new ItemTask().runTick();
        new BiomeTask().runTick();
        new AdvancementTask().runTick();
        new HeightTask().runTick();
        new CoordsTask().runTick();
        new StructureTask().runTick();
        walking.tick();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        KatzePlayer katzePlayer = KatzePlayer.adapt(event.getPlayer());

        updateAllTablists();

        boolean firstStartup = KatzeGames.get().getConfig().getBoolean("first-startup", true);
        if (firstStartup && PermissionDefault.OP.getValue(event.getPlayer().isOp())) {
            katzePlayer.sendMessage(Component.text("----\nIt seems to be the first time you start the plugin.\n-> Use /menu to adjust settings and /start to start the battle.\n----"));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        updateAllTablists();
    }

    public void startGame() {
        startPhase = false;

        updateAllTablists();

        for (KatzePlayer katzePlayer : KatzePlayer.getOnlinePlayers()) {
            ForceBattlePlayer player = (ForceBattlePlayer) katzePlayer;
            if (player.getPlayer() == null || player.isSpectator()) continue;

            if (player.getObjective() == null) {
                player.updateObjective(false, false);
            }
            if (player.isInTeam() && player.getTeam().getObjective() == null) {
                player.getTeam().updateObjective(player, false, false);
            }

            player.getPlayer().getInventory().addItem(Joker.getPlayerSkipItem(KatzeGames.get().getConfig().getInt("playerSkips", 3)));
            player.getPlayer().getInventory().addItem(Joker.getSwapItem(KatzeGames.get().getConfig().getInt("swaps", 1)));

            if (SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE) && player.isInTeam()) {
                player.getPlayer().getInventory().addItem(Joker.getTeamSkipItem(KatzeGames.get().getConfig().getInt("teamSkips", 1)));
            }
        }

        for (KatzeTeam team : KatzeTeam.teams) {
            if (team instanceof ForceBattleTeam fbTeam) {
                fbTeam.createBackpackGUIs();
            }
        }
    }

    public void handleBattleEnd() {
        KatzeGames.broadcast(Component.text("The battle is over. /displayresults (player/team) | /result (player/team)."));

        for (KatzePlayer player : KatzePlayer.getOnlinePlayers()) {
            if (player.getPlayer() == null) return;

            if (player.isInTeam()) ((ForceBattleTeam) player.getTeam()).setObjective(null);
            player.setCurrentObjective(null, false, false);
            player.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
            player.getPlayer().setHealth(20);
            player.getPlayer().setSaturation(20);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!KatzeGames.getTimer().isRunning() || SettingsManager.isEnabled(GlobalSetting.NO_HUNGER)) {
            event.setCancelled(true);

            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }


    @EventHandler
    public void onTeamJoin(PlayerTeamJoinEvent event) {
        updateAllTablists();
    }

    @EventHandler
    public void onTeamLeave(PlayerTeamLeaveEvent event) {
        updateAllTablists();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!KatzeGames.getTimer().isRunning()) return;

        MobsTask.onDamage(event);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!KatzeGames.getTimer().isRunning() || SettingsManager.isEnabled(GlobalSetting.NO_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    private void updateAllTablists() {
        // The tab list shows higher orders first and Paper refuses negative ones, so teams start at 1 to keep the spectators' 0 below team 0.
        KatzePlayer.getOnlinePlayers().forEach(player -> {
            if (player.isSpectator()) {
                player.getPlayer().setPlayerListOrder(0);
            } else if (player.isInTeam()) {
                player.getPlayer().setPlayerListOrder(player.getTeam().getId() + 1);
            } else {
                player.getPlayer().setPlayerListOrder(999);
            }
        });
    }

    @EventHandler
    public void onPlayerSkip(@NotNull PlayerInteractEvent event) {
        if (!(KatzePlayer.adapt(event.getPlayer()) instanceof ForceBattlePlayer player)) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) {return;}
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {return;}
        if (event.getItem() == null || !Joker.isPlayerSkipItem(event.getItem())) {return;}
        if (!player.isParticipating()) return;

        event.setCancelled(true);

        if (!KatzeGames.getTimer().isRunning()) {
            player.sendMessage(Component.text("Can't skip while timer is paused."));
            return;
        }
        if (player.getObjective() == null) {
            player.sendMessage(Component.text("You don't have an objective to skip."));
            return;
        }

        if (event.getPlayer().hasCooldown(event.getItem().getType())) return;
        event.getPlayer().setCooldown(event.getItem().getType(), 2);

        ForceBattleObjective oldObjective = player.getObjective();

        player.sendMessage(Component.text("You skipped " + StringUtility.formatName(oldObjective.getObjectiveString()) + "."));
        player.updateObjective(true, true);

        player.addSkippedObjectiveToList(oldObjective);

        decreaseItemAmount(event);

        if (oldObjective.getBattleType() == ForceBattleType.FORCE_ITEM) {
            Material material = oldObjective.getObjectiveValue(Material.class);
            if (material != null) {
                ItemStack itemStack = new ItemStack(material);
                player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), itemStack);
            }
        }
    }

    @EventHandler
    public void onTeamSkip(@NotNull PlayerInteractEvent event) {
        if (!(KatzePlayer.adapt(event.getPlayer()) instanceof ForceBattlePlayer katzePlayer)) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) {return;}
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {return;}
        if (event.getItem() == null || !Joker.isTeamSkipItem(event.getItem())) {return;}
        ForceBattleTeam team = (ForceBattleTeam) katzePlayer.getTeam();
        if (!katzePlayer.isParticipating()) return;

        event.setCancelled(true);

        if (!KatzeGames.getTimer().isRunning()) {
            katzePlayer.sendMessage(Component.text("Can't skip while timer is paused."));
            return;
        }
        if (team.getObjective() == null) {
            katzePlayer.sendMessage(Component.text("You don't have an objective to skip."));
            return;
        }

        if (event.getPlayer().hasCooldown(event.getItem().getType())) return;
        event.getPlayer().setCooldown(event.getItem().getType(), 2);

        ForceBattleObjective oldObjective = team.getObjective();

        team.getPlayers().forEach(player -> {
            if (player != katzePlayer) {
                player.sendMessage(Component.text(katzePlayer.getName() + " has skipped " + StringUtility.formatName(oldObjective.getObjectiveString()) + "."));
            }
        });

        katzePlayer.sendMessage(Component.text("You skipped " + StringUtility.formatName(oldObjective.getObjectiveString()) + "."));

        team.updateObjective(katzePlayer, true, true);

        katzePlayer.addSkippedObjectiveToList(oldObjective);

        decreaseItemAmount(event);

        if (oldObjective.getBattleType() == ForceBattleType.FORCE_ITEM) {
            Material material = oldObjective.getObjectiveValue(Material.class);
            if (material != null) {
                ItemStack itemStack = new ItemStack(material);
                katzePlayer.getPlayer().getWorld().dropItemNaturally(katzePlayer.getPlayer().getLocation(), itemStack);
            }
        }
    }

    @EventHandler
    public void onPlayerSwap(@NotNull PlayerInteractEvent event) {
        if (!(KatzePlayer.adapt(event.getPlayer()) instanceof ForceBattlePlayer katzePlayer)) return;
        if (event.getHand() == null || !event.getHand().equals(EquipmentSlot.HAND)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getItem() == null || !Joker.isSwapItem(event.getItem())) return;
        if (!katzePlayer.isParticipating()) return;
        event.setCancelled(true);

        if (!KatzeGames.getTimer().isRunning()) {
            katzePlayer.sendMessage(Component.text("Can't swap while timer is paused."));
            return;
        }

        if (katzePlayer.getObjective() == null) {
            katzePlayer.sendMessage(Component.text("There is no player to swap your objective with."));
            return;
        }

        List<KatzePlayer> availablePlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (event.getPlayer().equals(player)) continue;
            if (!(KatzePlayer.adapt(player) instanceof ForceBattlePlayer toSwap) || toSwap.isSpectator()) continue;
            ForceBattlePlayer sender = katzePlayer;
            if (toSwap.getObjective() == null) continue;
            if (toSwap.getTeam() == sender.getTeam() && toSwap.isInTeam() && sender.isInTeam()) continue;
            availablePlayers.add(toSwap);
        }

        if (availablePlayers.isEmpty()) {
            katzePlayer.sendMessage(Component.text("There is no player to swap your objective with."));
            return;
        }

        if (event.getPlayer().hasCooldown(event.getItem().getType())) return;
        event.getPlayer().setCooldown(event.getItem().getType(), 2);

        decreaseItemAmount(event);
        Random random = new Random();
        ForceBattlePlayer targetPlayer = (ForceBattlePlayer) availablePlayers.get(random.nextInt(availablePlayers.size()));

        ForceBattleObjective playerObjective = katzePlayer.getObjective();
        ForceBattleObjective targetObjective = targetPlayer.getObjective();

        katzePlayer.setCurrentObjective(targetObjective, false, false);
        targetPlayer.setCurrentObjective(playerObjective, false, false);

        targetPlayer.sendMessage(Component.text(katzePlayer.getName() + " swapped their objective with yours."));

        katzePlayer.sendMessage(Component.text("Your objective has been swapped with " + targetPlayer.getName() + "."));

        targetPlayer.getPlayer().playSound(targetPlayer.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuideBookUse(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.useItemInHand() != Event.Result.DENY) return;
        ItemStack item = event.getItem();
        if (item == null || !ForceBattleGuideBook.isGuideBook(item)) return;

        event.getPlayer().openBook(item);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(KatzePlayer.adapt(event.getEntity()) instanceof ForceBattlePlayer katzePlayer)) return;
        if (!KatzeGames.getTimer().isRunning() || !katzePlayer.isParticipating()) return;

        DeathTask.onDeath(event);

        List<ItemStack> jokers = new ArrayList<>();
        event.getDrops().removeIf(item -> {
            if (item == null) return false;
            if (!Joker.isPlayerSkipItem(item) && !Joker.isTeamSkipItem(item) && !Joker.isSwapItem(item)) return false;
            jokers.add(item.clone());
            return true;
        });

        if (!jokers.isEmpty()) {
            pendingItemRestores.put(event.getEntity().getUniqueId(), jokers);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        List<ItemStack> jokers = pendingItemRestores.remove(event.getPlayer().getUniqueId());
        if (jokers == null || jokers.isEmpty()) return;

        Bukkit.getScheduler().runTask(KatzeGames.get(), () ->
                event.getPlayer().getInventory().addItem(jokers.toArray(new ItemStack[0]))
        );
    }

    private void decreaseItemAmount(@NotNull PlayerInteractEvent event) {
        Inventory inventory = event.getPlayer().getInventory();
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        ItemStack stack = inventory.getItem(slot);
        if (stack != null) {
            stack.setAmount(stack.getAmount() - 1);
            inventory.setItem(slot, stack);
            event.getPlayer().setCooldown(stack.getType(), 20);
        }
    }
}

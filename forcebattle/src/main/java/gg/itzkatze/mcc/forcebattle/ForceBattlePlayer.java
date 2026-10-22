package gg.itzkatze.mcc.forcebattle;

import gg.itzkatze.mcc.challenges.ChallengeObjective;
import gg.itzkatze.mcc.challenges.ChallengeType;
import gg.itzkatze.mcc.configuration.SettingsManager;
import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.event.ObjectiveUpdateEvent;
import gg.itzkatze.mcc.event.PlayerResetEvent;
import gg.itzkatze.mcc.forcebattle.gui.TeamBackpackGUI;
import gg.itzkatze.mcc.game.SessionPersistence;
import gg.itzkatze.mcc.level.LevelStore;
import gg.itzkatze.mcc.team.MccTeam;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.Joker;
import gg.itzkatze.mcc.util.StringUtility;
import gg.itzkatze.mcc.util.Toast;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ForceBattlePlayer extends MccPlayer {

    private static final Logger logger = LoggerFactory.getLogger("ForceBattle/" + ForceBattlePlayer.class.getSimpleName());

    private final Inventory BACKPACK_INVENTORY;

    @Setter
    @Getter
    private ForceBattleObjective objective;
    @Getter
    @Setter
    private List<ForceBattleObjective> skippedObjectives = new ArrayList<>();
    @Getter
    @Setter
    private int chainProgress = 0;
    private int points = 0;

    public ForceBattlePlayer(@NotNull UUID uuid, @NotNull String name) {
        super(uuid, name);
        this.BACKPACK_INVENTORY = Bukkit.createInventory(null, 27, name + "'s Backpack");
    }

    /**
     * Returns the Force Battle player for the given Bukkit player if they are currently
     * represented as a {@link ForceBattlePlayer}.
     */
    public static @NotNull Optional<ForceBattlePlayer> from(Player player) {
        MccPlayer mcc = MccPlayer.adapt(player);
        return mcc instanceof ForceBattlePlayer fp ? Optional.of(fp) : Optional.empty();
    }

    /**
     * Returns the Force Battle player for the given name if they are currently
     * represented as a {@link ForceBattlePlayer}.
     */
    public static @NotNull Optional<ForceBattlePlayer> from(String name) {
        return MccPlayer.adapt(name)
                .filter(ForceBattlePlayer.class::isInstance)
                .map(ForceBattlePlayer.class::cast);
    }

    public void updateObjective(boolean finishLast, boolean hasBeenSkipped) {
        if (finishLast && this.objective != null) {
            finishObjective(hasBeenSkipped);
        }

        ForceBattleObjective newObjective = ForceBattleObjectiveManager.getInstance().getNewObjective(this);

        ObjectiveUpdateEvent updateEvent = new ObjectiveUpdateEvent(this, newObjective);
        Bukkit.getPluginManager().callEvent(updateEvent);
        if (updateEvent.isCancelled()) {
            if (newObjective != null) newObjective.delete();
            logger.info("ObjectiveUpdateEvent has been denied by an external plugin.");
            return;
        }

        setCurrentObjective((ForceBattleObjective) updateEvent.getNewObjective(), false, false);

        if (SettingsManager.isEnabled(ForceBattleSettings.CHAIN_MODE)) {
            increaseChainProgress();
        }
    }

    @Override
    public void setCurrentObjective(ChallengeObjective newObjective, boolean finishLast, boolean hasBeenSkipped) {
        setCurrentObjective(newObjective instanceof ForceBattleObjective o ? o : null, finishLast, hasBeenSkipped);
    }

    public void setCurrentObjective(ForceBattleObjective newObjective, boolean finishLast, boolean hasBeenSkipped) {
        if (finishLast && this.objective != null) {
            finishObjective(hasBeenSkipped);
        }

        if (MinecraftChallenges.getTimer().isRunning() && newObjective != null) {
            Material toastIcon = newObjective.getToastIcon();
            Component toastMessage = Component.text("New Objective: ").color(NamedTextColor.GRAY)
                    .append(Component.text(StringUtility.formatName(newObjective.getObjectiveString())).color(NamedTextColor.BLUE));
            sendMessage(Component.text("Next objective: ")
                    .append(Component.text(StringUtility.formatName(newObjective.getObjectiveString())).color(NamedTextColor.BLUE))
                    .append(Component.text(" (" + newObjective.getBattleType().getPrefix() + ").")).color(NamedTextColor.GRAY));
            if (getPlayer() != null) {
                Bukkit.getScheduler().runTaskLater(
                        MinecraftChallenges.get(),
                        () -> Toast.display(getPlayer(), toastIcon, toastMessage, Toast.Style.CHALLENGE, MinecraftChallenges.get()),
                        2L
                );
            }
        }

        this.objective = newObjective;
        SessionPersistence.requestSave();
    }

    private void finishObjective(boolean hasBeenSkipped) {
        this.objective.setFinished(this);
        this.objective.setHasBeenSkipped(hasBeenSkipped);
        this.points++;
        if (!hasBeenSkipped && isParticipating()) {
            LevelStore.get().grant(this, ForceBattleXp.OBJECTIVE.xp());
        }
    }

    public void increaseChainProgress() {
        chainProgress++;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public @NonNull Optional<ChallengeType> getChallengeType() {
        return Optional.of(ChallengeType.FORCE_BATTLE);
    }

    @Override
    public void addSkippedObjectiveToList(ChallengeObjective objective) {
        if (objective instanceof ForceBattleObjective o) {
            skippedObjectives.add(o);
        }
    }

    public void reset(boolean newObjective) {
        PlayerResetEvent resetEvent = new PlayerResetEvent(this);
        Bukkit.getPluginManager().callEvent(resetEvent);
        if (resetEvent.isCancelled()) {
            logger.info("PlayerResetEvent has been denied by an external plugin.");
            return;
        }

        this.points = 0;
        this.chainProgress = 0;
        this.skippedObjectives = new ArrayList<>();
        setCurrentObjective(null, false, false);
        if (isInTeam() && getTeam() instanceof ForceBattleTeam fbTeam) {
            fbTeam.setChainProgress(0);
        }
        ForceBattleObjective.finished().forEach(objective -> {
            if (objective.getWhoFinished().equals(this)) {
                objective.delete();
            }
        });
        if (newObjective) {
            updateObjective(false, false);
        }

        handleReset();
    }

    public void openBackpack() {
        if (isInTeam()) {
            MccTeam team = getTeam();
            if (team instanceof ForceBattleTeam fbTeam) {
                fbTeam.getBACKPACK_INVENTORIES().getFirst().open(this);
            }
        } else {
            openInventory(getBackpack());
        }
    }

    public void openBackpack(MccPlayer viewer) {
        viewer.openInventory(getBackpack());
    }

    public void addSkippedObjectiveToList(ForceBattleObjective objective) {
        skippedObjectives.add(objective);
    }

    public void addPlayerSkip(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().addItem(Joker.getPlayerSkipItem(amount));
    }

    public void addTeamSkip(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().addItem(Joker.getTeamSkipItem(amount));
    }

    public void addSwap(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().addItem(Joker.getSwapItem(amount));
    }

    public void removePlayerSkip(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().removeItem(Joker.getPlayerSkipItem(amount));
    }

    public void removeTeamSkip(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().removeItem(Joker.getTeamSkipItem(amount));
    }

    public void removeSwap(final int amount) {
        if (getPlayer() == null) return;
        getPlayer().getInventory().removeItem(Joker.getSwapItem(amount));
    }

    @Override
    public Inventory getBackpack() {
        return BACKPACK_INVENTORY;
    }

    @Override
    public @Nullable List<ItemStack> getBackpackContents() {
        if (!SettingsManager.isEnabled(ForceBattleSettings.BACKPACK)) return null;
        if (getTeam() instanceof ForceBattleTeam team) {
            List<ItemStack> contents = new ArrayList<>();
            for (TeamBackpackGUI page : team.getBACKPACK_INVENTORIES()) {
                contents.addAll(copyContents(page.getInventory(), slot -> !page.isNavigationSlot(slot)));
            }
            return contents;
        }
        return super.getBackpackContents();
    }
}

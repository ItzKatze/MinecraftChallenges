package gg.itzkatze.katze.forcebattle.gui;

import gg.itzkatze.katze.data.DamageTypeSimplified;
import gg.itzkatze.katze.data.KatzeDamageType;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.MobsSimplified;
import gg.itzkatze.katze.forcebattle.ForceBattleObjective;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.util.KatzeColor;
import lombok.Getter;
import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.time.Duration;

public class ResultGUI extends KatzeGUI {

    private static final int SPACES_ON_PAGE = 45;

    private final ResultType resultType;
    private final int page;
    private final KatzePlayer targetPlayer;
    private final KatzeTeam targetTeam;

    private final boolean animated;
    private final boolean skipLoad;

    private final Map<KatzePlayer, Integer> playerPageCache = new HashMap<>();
    private final Map<KatzePlayer, List<ForceBattleObjective>> objectiveList = new HashMap<>();
    private final Map<KatzePlayer, BukkitRunnable> activeAnimations = new HashMap<>();

    public ResultGUI(ResultType resultType, int page, KatzePlayer targetPlayer, KatzeTeam targetTeam, boolean animated, boolean skipLoad) {
        super(generateTitle(resultType, targetPlayer, targetTeam, animated), 54);
        this.page = page;
        this.resultType = resultType;
        this.targetPlayer = targetPlayer;
        this.targetTeam = targetTeam;
        this.animated = animated;
        this.skipLoad = skipLoad;
    }

    public ResultGUI(ResultType resultType, int page, KatzePlayer targetPlayer, KatzeTeam targetTeam, boolean animated) {
        this(resultType, page, targetPlayer, targetTeam, animated, false);
    }

    public ResultGUI(KatzePlayer targetPlayer, boolean animated) {
        this(ResultType.PLAYER, 1, targetPlayer, null, animated, false);
    }

    public ResultGUI(KatzeTeam targetTeam, boolean animated) {
        this(ResultType.TEAM, 1, null, targetTeam, animated, false);
    }

    public ResultGUI() {
        this(ResultType.ALL, 1, null, null, false, false);
    }

    @Override
    public void open(KatzePlayer viewer) {
        if (skipLoad) {
            super.open(viewer);

            List<ForceBattleObjective> objectives = objectiveList.get(viewer);
            if (objectives == null) return;

            if (!animated) setItems(viewer);
            return;
        }

        Collection<ForceBattleObjective> collection = switch (resultType) {
            case PLAYER -> ForceBattleObjective.finishedBy(targetPlayer);
            case TEAM -> ResultCommandUtils.combineTeamObjectives(targetTeam);
            case ALL -> ForceBattleObjective.finished();
        };

        List<ForceBattleObjective> objectives = new ArrayList<>(collection);
        objectiveList.put(viewer, objectives);
        playerPageCache.put(viewer, page);

        super.open(viewer);

        if (animated) startAnimation(viewer, objectives, 0);
        else setItems(viewer);
    }

    private void startAnimation(KatzePlayer viewer, List<ForceBattleObjective> objectives, int startIndex) {
        fill(ItemStackCreator.fillerItem());

        BukkitRunnable task = new BukkitRunnable() {
            int index = startIndex;

            @Override
            public void run() {
                if (viewer.isOffline() || !viewer.getPlayer().getOpenInventory().getTopInventory().equals(getInventory())) {
                    cancel();
                    activeAnimations.remove(viewer);
                    return;
                }

                if (index >= objectives.size()) {
                    cancel();
                    activeAnimations.remove(viewer);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            viewer.getPlayer().playSound(viewer.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1f);
                            viewer.getPlayer().closeInventory();
                            showResultSummary(viewer);
                        }
                    }.runTaskLater(KatzeGames.get(), 20L);
                    return;
                }

                int currentPage = (index / SPACES_ON_PAGE) + 1;
                int slot = 9 + (index % SPACES_ON_PAGE);

                ForceBattleObjective objective = objectives.get(index);

                if (currentPage != page) {
                    ResultGUI nextPageGUI = new ResultGUI(resultType, currentPage, targetPlayer, targetTeam, true, true);
                    nextPageGUI.objectiveList.put(viewer, objectives);
                    nextPageGUI.playerPageCache.put(viewer, currentPage);

                    nextPageGUI.open(viewer);
                    nextPageGUI.startAnimation(viewer, objectives, index);

                    cancel();
                    return;
                }

                set(new GUIItem(slot) {
                    @Override
                    public ItemStack getItem(KatzePlayer player) {
                        return ResultCommandUtils.buildObjectiveItem(objective, false);
                    }
                });

                updateInventory(viewer);
                index++;
            }
        };

        final int animationDelayTicks = 15;
        task.runTaskTimer(KatzeGames.get(), 0L, animationDelayTicks);
        activeAnimations.put(viewer, task);
    }

    @Override
    public void setItems(KatzePlayer viewer) {
        if (animated) return;

        int currentPage = playerPageCache.getOrDefault(viewer, 1);
        List<ForceBattleObjective> objectives = objectiveList.get(viewer);
        if (objectives == null) return;

        fill(ItemStackCreator.fillerItem());

        int totalPages = (int) Math.ceil((double) objectives.size() / SPACES_ON_PAGE);

        if (currentPage > 1) {
            set(GUIClickableItem.getGoBackItem(0, new ResultGUI(resultType, currentPage - 1, targetPlayer, targetTeam, false)));
        }

        if (currentPage < totalPages) {
            set(GUIClickableItem.getGoForthItem(8, new ResultGUI(resultType, currentPage + 1, targetPlayer, targetTeam, false)));
        }

        int startIndex = (currentPage - 1) * SPACES_ON_PAGE;
        for (int i = 0; i < SPACES_ON_PAGE; i++) {
            int index = startIndex + i;
            int slot = 9 + i;
            if (index >= objectives.size()) break;

            ForceBattleObjective objective = objectives.get(index);
            set(new GUIItem(slot) {
                @Override
                public ItemStack getItem(KatzePlayer player) {
                    return ResultCommandUtils.buildObjectiveItem(objective, true);
                }
            });
        }
    }

    private void showResultSummary(KatzePlayer viewer) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        switch (resultType) {
            case PLAYER -> {
                if (targetPlayer != null) {
                    viewer.sendMessage(ResultUtils.buildPlayerResultMessage(targetPlayer));

                    viewer.getPlayer().showTitle(Title.title(
                            Component.text(targetPlayer.getName(), KatzeColor.GOLD),
                            Component.text("Score: ", KatzeColor.YELLOW)
                                    .append(Component.text(targetPlayer.getPoints(), KatzeColor.LIGHT_BLUE)),
                            times
                    ));
                }
            }
            case TEAM -> {
                if (targetTeam != null) {
                    viewer.sendMessage(ResultUtils.buildTeamResultMessage(targetTeam));

                    viewer.getPlayer().showTitle(Title.title(
                            Component.text("Team " + targetTeam.getId(), KatzeColor.GOLD),
                            Component.text("Score: ", KatzeColor.YELLOW)
                                    .append(Component.text(targetTeam.getPoints(), KatzeColor.LIGHT_BLUE)),
                            times
                    ));
                }
            }
            case ALL -> {
                viewer.getPlayer().showTitle(Title.title(
                        Component.text("All Results", KatzeColor.LIGHT_BLUE),
                        Component.text("Animation complete", KatzeColor.LIGHT_GRAY),
                        times
                ));
            }
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    public void onClose(KatzePlayer player, InventoryCloseEvent event) {
        BukkitRunnable task = activeAnimations.remove(player);
        if (task != null) task.cancel();

        objectiveList.remove(player);
        playerPageCache.remove(player);
    }

    private static String generateTitle(ResultType type, KatzePlayer player, KatzeTeam team, boolean animated) {
        if (animated) {
            return "Results";
        }

        return switch (type) {
            case PLAYER -> "Results | " + (player != null ? player.getName() : "Unknown");
            case TEAM -> "Results | Team " + (team != null ? team.getId() : "N/A");
            default -> "Results | All Players";
        };
    }

    @Getter
    public enum ResultType {
        ALL("gui.result_title_all"),
        TEAM("gui.result_title_team"),
        PLAYER("gui.result_title_player");

        private final String titleKey;

        ResultType(String titleKey) {
            this.titleKey = titleKey;
        }
    }

    public static class ResultCommandUtils {

        public static @NotNull List<ForceBattleObjective> combineTeamObjectives(@NotNull KatzeTeam team) {
            List<ForceBattleObjective> objectives = new ArrayList<>();
            team.getPlayers().forEach(teamPlayer -> objectives.addAll(ForceBattleObjective.finishedBy(teamPlayer)));
            objectives.sort(Comparator.comparingInt(ForceBattleObjective::getTime).reversed());
            return objectives;
        }

        public static @NotNull ItemStack buildObjectiveItem(@NotNull ForceBattleObjective objective, boolean displayNames) {
            Material material = Material.STONE;
            Object obj = objective.getObjectiveValue();

            if (obj instanceof Material m) material = m;
            else if (obj instanceof EntityType || obj instanceof MobsSimplified) material = Material.SPIDER_SPAWN_EGG;
            else if (obj instanceof Biome || obj instanceof BiomeSimplified) material = Material.GRASS_BLOCK;
            else if (obj instanceof KatzeAdvancement) material = Material.BREWING_STAND;
            else if (obj instanceof Integer) material = Material.SCAFFOLDING;
            else if (obj instanceof Location) material = Material.DIAMOND_BOOTS;
            else if (obj instanceof KatzeStructure || obj instanceof StructureSimplified) material = Material.STRUCTURE_BLOCK;
            else if (obj instanceof KatzeDamageType || obj instanceof DamageTypeSimplified) material =  Material.SKELETON_SKULL;

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Time: ").color(KatzeColor.LIGHT_GRAY)
                    .append(Component.text(StringUtility.formatTime(objective.getTime()))
                            .color(KatzeColor.BLUE)));

            if (displayNames) {
                lore.add(Component.text("Player: ").color(KatzeColor.LIGHT_GRAY)
                        .append(Component.text(objective.getWhoFinished().getName())
                                .color(KatzeColor.BLUE)));
            }
            if (objective.hasBeenSkipped()) {
                lore.add(Component.empty());
                lore.add(Component.text("SKIPPED").color(KatzeColor.RED));
            }

            Component name = Component.text(objective.getBattleType().getPrefix() + ": ")
                    .color(KatzeColor.LIGHT_GRAY)
                    .append(Component.text(StringUtility.formatName(objective.getObjectiveString()))
                            .color(KatzeColor.BLUE));

            return ItemStackCreator.getStack(name, material, lore);
        }
    }
}

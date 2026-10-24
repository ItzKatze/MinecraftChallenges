package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.forcebattle.ForceBattleObjectiveManager.ObjectiveSeed;
import gg.itzkatze.katze.forcebattle.ObjectiveValueCodec.EncodedValue;
import gg.itzkatze.katze.forcebattle.gui.TeamBackpackGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.util.WorldStorage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntPredicate;

public final class ForceBattlePersistence {

    private static final Logger logger = LoggerFactory.getLogger("ForceBattle/" + ForceBattlePersistence.class.getSimpleName());
    private static final String FILE_NAME = "forcebattle_data.json";

    private ForceBattlePersistence() {
    }

    public static void saveData() {
        WorldStorage.save(FILE_NAME, snapshot());
    }

    public static void loadData() {
        SaveData data = WorldStorage.load(FILE_NAME, SaveData.class);
        if (data == null) {
            return;
        }
        try {
            restore(data);
        } catch (RuntimeException e) {
            logger.warn("Failed to restore the ForceBattle data.", e);
        }
    }

    public static void deleteData() {
        WorldStorage.delete(FILE_NAME);
    }

    private static @NotNull SaveData snapshot() {
        SaveData data = new SaveData();
        data.startPhase = ForceBattleGameListener.startPhase;

        ObjectiveTable table = new ObjectiveTable(data.objectives);
        ForceBattleObjective.all().forEach(table::indexOf);

        for (KatzePlayer player : KatzePlayer.PLAYERS) {
            if (player instanceof ForceBattlePlayer fbPlayer) {
                data.players.add(PlayerData.of(fbPlayer, table));
            }
        }
        for (KatzeTeam team : KatzeTeam.teams) {
            if (team instanceof ForceBattleTeam fbTeam) {
                data.teams.add(TeamData.of(fbTeam, table));
            }
        }

        ForceBattleObjectiveManager objectiveManager = ForceBattleObjectiveManager.getInstance();
        data.chain = encodeSeeds(objectiveManager.exportChain());
        data.teamChain = encodeSeeds(objectiveManager.exportTeamChain());
        return data;
    }

    private static void restore(@NotNull SaveData data) {
        ForceBattleGameListener.startPhase = data.startPhase;

        ForceBattleObjective.deleteAll();
        List<ForceBattleObjective> objectives = new ArrayList<>();
        for (ObjectiveData objectiveData : nonNull(data.objectives)) {
            objectives.add(restoreObjective(objectiveData));
        }

        for (PlayerData playerData : nonNull(data.players)) {
            restorePlayer(playerData, objectives);
        }
        for (TeamData teamData : nonNull(data.teams)) {
            restoreTeam(teamData, objectives);
        }

        ForceBattleObjectiveManager.getInstance().importChains(decodeSeeds(data.chain), decodeSeeds(data.teamChain));
    }

    private static @Nullable ForceBattleObjective restoreObjective(@NotNull ObjectiveData data) {
        ForceBattleType type = parseType(data.type);
        Object value = ObjectiveValueCodec.decode(data.value);
        if (type == null || value == null) {
            logger.warn("Dropping saved objective {} {} because it can't be restored.", data.type, describe(data.value));
            return null;
        }

        ForceBattleObjective objective = new ForceBattleObjective(type, value);
        objective.setHasBeenSkipped(data.skipped);
        Optional<KatzePlayer> finisher = findPlayer(data.finishedBy);
        if (finisher.isPresent() && data.time != null) {
            objective.restoreFinished(finisher.get(), data.time);
        }
        return objective;
    }

    private static void restorePlayer(@NotNull PlayerData data, @NotNull List<ForceBattleObjective> objectives) {
        if (!(findPlayer(data.uuid).orElse(null) instanceof ForceBattlePlayer player)) {
            return;
        }

        player.setPoints(data.points);
        player.setChainProgress(data.chainProgress);
        player.setObjective(objectiveAt(objectives, data.current));

        List<ForceBattleObjective> skippedObjectives = new ArrayList<>();
        for (Integer index : nonNull(data.skipped)) {
            ForceBattleObjective objective = objectiveAt(objectives, index);
            if (objective != null) {
                skippedObjectives.add(objective);
            }
        }
        player.setSkippedObjectives(skippedObjectives);

        fillInventory(player.getBackpack(), decodeItems(data.backpack), slot -> false);
    }

    private static void restoreTeam(@NotNull TeamData data, @NotNull List<ForceBattleObjective> objectives) {
        ForceBattleTeam team = ForceBattleTeam.findById(data.id).orElse(null);
        if (team == null) {
            return;
        }

        team.setChainProgress(data.chainProgress);
        team.setObjective(objectiveAt(objectives, data.current));

        List<String> pages = nonNull(data.backpack);
        if (pages.isEmpty()) {
            return;
        }
        team.createBackpackGUIs();
        List<TeamBackpackGUI> backpackPages = team.getBACKPACK_INVENTORIES();
        for (int i = 0; i < Math.min(pages.size(), backpackPages.size()); i++) {
            TeamBackpackGUI page = backpackPages.get(i);
            fillInventory(page.getInventory(), decodeItems(pages.get(i)), page::isNavigationSlot);
        }
    }

    private static @NotNull List<SeedData> encodeSeeds(@NotNull List<ObjectiveSeed> seeds) {
        List<SeedData> encoded = new ArrayList<>();
        for (ObjectiveSeed seed : seeds) {
            EncodedValue value = ObjectiveValueCodec.encode(seed.getValue());
            if (value != null) {
                encoded.add(new SeedData(seed.getType().name(), value));
            }
        }
        return encoded;
    }

    private static @NotNull List<ObjectiveSeed> decodeSeeds(@Nullable List<SeedData> seeds) {
        List<ObjectiveSeed> decoded = new ArrayList<>();
        for (SeedData seed : nonNull(seeds)) {
            ForceBattleType type = parseType(seed.type);
            Object value = ObjectiveValueCodec.decode(seed.value);
            if (type == null || value == null) {
                logger.warn("Dropping saved chain objective {} {} because it can't be restored.", seed.type, describe(seed.value));
                continue;
            }
            decoded.add(new ObjectiveSeed(type, value));
        }
        return decoded;
    }

    private static @NotNull String encodeItems(@Nullable ItemStack @NotNull [] items) {
        return Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(items));
    }

    private static @NotNull ItemStack @NotNull [] decodeItems(@Nullable String encoded) {
        if (encoded == null) {
            return new ItemStack[0];
        }
        try {
            return ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(encoded));
        } catch (RuntimeException e) {
            logger.warn("Failed to restore backpack contents, leaving them empty.", e);
            return new ItemStack[0];
        }
    }

    private static void fillInventory(@NotNull Inventory inventory, @NotNull ItemStack @NotNull [] items, @NotNull IntPredicate skippedSlot) {
        for (int slot = 0; slot < Math.min(items.length, inventory.getSize()); slot++) {
            if (!skippedSlot.test(slot) && !items[slot].isEmpty()) {
                inventory.setItem(slot, items[slot]);
            }
        }
    }

    private static @Nullable ForceBattleObjective objectiveAt(@NotNull List<ForceBattleObjective> objectives, @Nullable Integer index) {
        if (index == null || index < 0 || index >= objectives.size()) {
            return null;
        }
        return objectives.get(index);
    }

    private static @NotNull Optional<KatzePlayer> findPlayer(@Nullable String uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        try {
            return KatzePlayer.adapt(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static @Nullable ForceBattleType parseType(@Nullable String name) {
        if (name == null) {
            return null;
        }
        try {
            return ForceBattleType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static @NotNull String describe(@Nullable EncodedValue value) {
        return value == null ? "without value" : value.kind + " " + value.raw;
    }

    private static <T> @NotNull List<T> nonNull(@Nullable List<T> list) {
        return list == null ? List.of() : list;
    }

    private static final class ObjectiveTable {
        private final List<ObjectiveData> entries;
        private final Map<ForceBattleObjective, Integer> indices = new IdentityHashMap<>();

        private ObjectiveTable(@NotNull List<ObjectiveData> entries) {
            this.entries = entries;
        }

        @Nullable Integer indexOf(@Nullable ForceBattleObjective objective) {
            if (objective == null) {
                return null;
            }
            return indices.computeIfAbsent(objective, key -> {
                entries.add(ObjectiveData.of(key));
                return entries.size() - 1;
            });
        }
    }

    private static final class SaveData {
        boolean startPhase = true;
        List<ObjectiveData> objectives = new ArrayList<>();
        List<PlayerData> players = new ArrayList<>();
        List<TeamData> teams = new ArrayList<>();
        List<SeedData> chain = new ArrayList<>();
        List<SeedData> teamChain = new ArrayList<>();
    }

    private static final class ObjectiveData {
        String type;
        EncodedValue value;
        String finishedBy;
        Integer time;
        boolean skipped;

        static ObjectiveData of(@NotNull ForceBattleObjective objective) {
            ObjectiveData data = new ObjectiveData();
            data.type = objective.getBattleType().name();
            data.value = ObjectiveValueCodec.encode(objective.getObjectiveValue());
            if (objective.isFinished()) {
                data.finishedBy = objective.getWhoFinished().getUuid().toString();
                data.time = objective.getTime();
            }
            data.skipped = objective.hasBeenSkipped();
            return data;
        }
    }

    private static final class PlayerData {
        String uuid;
        int points;
        int chainProgress;
        Integer current;
        List<Integer> skipped = new ArrayList<>();
        String backpack;

        static PlayerData of(@NotNull ForceBattlePlayer player, @NotNull ObjectiveTable table) {
            PlayerData data = new PlayerData();
            data.uuid = player.getUuid().toString();
            data.points = player.getPoints();
            data.chainProgress = player.getChainProgress();
            data.current = table.indexOf(player.getObjective());
            for (ForceBattleObjective objective : player.getSkippedObjectives()) {
                data.skipped.add(table.indexOf(objective));
            }
            data.backpack = encodeItems(player.getBackpack().getContents());
            return data;
        }
    }

    private static final class TeamData {
        int id;
        int chainProgress;
        Integer current;
        List<String> backpack = new ArrayList<>();

        static TeamData of(@NotNull ForceBattleTeam team, @NotNull ObjectiveTable table) {
            TeamData data = new TeamData();
            data.id = team.getId();
            data.chainProgress = team.getChainProgress();
            data.current = table.indexOf(team.getObjective());
            for (TeamBackpackGUI page : team.getBACKPACK_INVENTORIES()) {
                ItemStack[] contents = page.getInventory().getContents();
                for (int slot = 0; slot < contents.length; slot++) {
                    if (page.isNavigationSlot(slot)) {
                        contents[slot] = null;
                    }
                }
                data.backpack.add(encodeItems(contents));
            }
            return data;
        }
    }

    private static final class SeedData {
        String type;
        EncodedValue value;

        private SeedData() {
        }

        SeedData(@NotNull String type, @NotNull EncodedValue value) {
            this.type = type;
            this.value = value;
        }
    }
}

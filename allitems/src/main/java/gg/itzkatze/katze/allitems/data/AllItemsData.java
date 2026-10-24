package gg.itzkatze.katze.allitems.data;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.allitems.AllItemsObjective;
import gg.itzkatze.katze.allitems.AllItemsObjectiveType;
import gg.itzkatze.katze.allitems.AllItemsSettings;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.util.StringUtility;
import gg.itzkatze.katze.util.WorldStorage;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AllItemsData {

    private static final String DATA_FILE_NAME = "all_items_progress.json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<Material, FoundEntry> teamFoundItems = new ConcurrentHashMap<>();
    private final Map<BiomeSimplified, FoundEntry> teamFoundBiomes = new ConcurrentHashMap<>();
    private final Map<EntityType, FoundEntry> teamFoundMobs = new ConcurrentHashMap<>();
    private final Map<StructureSimplified, FoundEntry> teamFoundStructures = new ConcurrentHashMap<>();
    private final Map<KatzeAdvancement, FoundEntry> teamFoundAdvancements = new ConcurrentHashMap<>();

    private final Map<UUID, Set<Material>> playerFoundItems = new ConcurrentHashMap<>();
    private final Map<UUID, Set<BiomeSimplified>> playerFoundBiomes = new ConcurrentHashMap<>();
    private final Map<UUID, Set<EntityType>> playerFoundMobs = new ConcurrentHashMap<>();
    private final Map<UUID, Set<StructureSimplified>> playerFoundStructures = new ConcurrentHashMap<>();
    private final Map<UUID, Set<KatzeAdvancement>> playerFoundAdvancements = new ConcurrentHashMap<>();

    private final List<FoundObjective> recentFinds = Collections.synchronizedList(new ArrayList<>());

    private final Map<UUID, ChainState> playerChains = new ConcurrentHashMap<>();
    private ChainState teamChain;

    public record FoundEntry(String playerName, UUID playerUUID, LocalDateTime foundTime) {

        public String getFormattedTime() {
            return foundTime.format(TIMESTAMP_FORMAT);
        }
    }

    public record FoundObjective(AllItemsObjectiveType type, String key, String displayName, FoundEntry entry) {
    }

    public boolean recordItemFound(@NotNull Player player, @NotNull Material material) {
        FoundEntry entry = buildEntry(player);

        if (AllItemsSettings.isTeamMode()) {
            if (teamFoundItems.containsKey(material)) {
                return false;
            }
            teamFoundItems.put(material, entry);
        } else if (hasPlayerFoundItem(player.getUniqueId(), material)) {
            return false;
        }

        playerFoundItems.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(material);
        addRecentFind(AllItemsObjectiveType.ITEM, material.name(), formatMaterialName(material), entry);
        advanceChainIfNeeded(player, AllItemsObjectiveType.ITEM, material.name());
        SessionPersistence.requestSave();
        return true;
    }

    public boolean recordBiomeFound(@NotNull Player player, @NotNull BiomeSimplified biome) {
        FoundEntry entry = buildEntry(player);

        if (AllItemsSettings.isTeamMode()) {
            if (teamFoundBiomes.containsKey(biome)) {
                return false;
            }
            teamFoundBiomes.put(biome, entry);
        } else if (hasPlayerFoundBiome(player.getUniqueId(), biome)) {
            return false;
        }

        playerFoundBiomes.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(biome);
        addRecentFind(AllItemsObjectiveType.BIOME, biome.name(), biome.getName(), entry);
        advanceChainIfNeeded(player, AllItemsObjectiveType.BIOME, biome.name());
        SessionPersistence.requestSave();
        return true;
    }

    public boolean recordMobFound(@NotNull Player player, @NotNull EntityType type) {
        FoundEntry entry = buildEntry(player);

        if (AllItemsSettings.isTeamMode()) {
            if (teamFoundMobs.containsKey(type)) {
                return false;
            }
            teamFoundMobs.put(type, entry);
        } else if (hasPlayerFoundMob(player.getUniqueId(), type)) {
            return false;
        }

        playerFoundMobs.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(type);
        addRecentFind(AllItemsObjectiveType.MOB, type.name(), formatMaterialName(type.name()), entry);
        advanceChainIfNeeded(player, AllItemsObjectiveType.MOB, type.name());
        SessionPersistence.requestSave();
        return true;
    }

    public boolean recordStructureFound(@NotNull Player player, @NotNull StructureSimplified structure) {
        FoundEntry entry = buildEntry(player);

        if (AllItemsSettings.isTeamMode()) {
            if (teamFoundStructures.containsKey(structure)) {
                return false;
            }
            teamFoundStructures.put(structure, entry);
        } else if (hasPlayerFoundStructure(player.getUniqueId(), structure)) {
            return false;
        }

        playerFoundStructures.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(structure);
        addRecentFind(AllItemsObjectiveType.STRUCTURE, structure.name(), structure.getName(), entry);
        advanceChainIfNeeded(player, AllItemsObjectiveType.STRUCTURE, structure.name());
        SessionPersistence.requestSave();
        return true;
    }

    public boolean recordAdvancementFound(@NotNull Player player, @NotNull KatzeAdvancement advancement) {
        FoundEntry entry = buildEntry(player);

        if (AllItemsSettings.isTeamMode()) {
            if (teamFoundAdvancements.containsKey(advancement)) {
                return false;
            }
            teamFoundAdvancements.put(advancement, entry);
        } else if (hasPlayerFoundAdvancement(player.getUniqueId(), advancement)) {
            return false;
        }

        playerFoundAdvancements.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet()).add(advancement);
        addRecentFind(AllItemsObjectiveType.ADVANCEMENT, advancement.name(), advancement.getName(), entry);
        advanceChainIfNeeded(player, AllItemsObjectiveType.ADVANCEMENT, advancement.name());
        SessionPersistence.requestSave();
        return true;
    }

    public boolean isItemFound(@NotNull Material material) {
        return teamFoundItems.containsKey(material);
    }

    public boolean isBiomeFound(@NotNull BiomeSimplified biome) {
        return teamFoundBiomes.containsKey(biome);
    }

    public boolean isMobFound(@NotNull EntityType type) {
        return teamFoundMobs.containsKey(type);
    }

    public boolean isStructureFound(@NotNull StructureSimplified structure) {
        return teamFoundStructures.containsKey(structure);
    }

    public boolean isAdvancementFound(@NotNull KatzeAdvancement advancement) {
        return teamFoundAdvancements.containsKey(advancement);
    }

    public boolean hasPlayerFoundItem(@NotNull UUID playerUUID, @NotNull Material material) {
        if (AllItemsSettings.isTeamMode()) {
            return isItemFound(material);
        }
        return playerFoundItems.getOrDefault(playerUUID, Collections.emptySet()).contains(material);
    }

    public boolean hasPlayerFoundBiome(@NotNull UUID playerUUID, @NotNull BiomeSimplified biome) {
        if (AllItemsSettings.isTeamMode()) {
            return isBiomeFound(biome);
        }
        return playerFoundBiomes.getOrDefault(playerUUID, Collections.emptySet()).contains(biome);
    }

    public boolean hasPlayerFoundMob(@NotNull UUID playerUUID, @NotNull EntityType type) {
        if (AllItemsSettings.isTeamMode()) {
            return isMobFound(type);
        }
        return playerFoundMobs.getOrDefault(playerUUID, Collections.emptySet()).contains(type);
    }

    public boolean hasPlayerFoundStructure(@NotNull UUID playerUUID, @NotNull StructureSimplified structure) {
        if (AllItemsSettings.isTeamMode()) {
            return isStructureFound(structure);
        }
        return playerFoundStructures.getOrDefault(playerUUID, Collections.emptySet()).contains(structure);
    }

    public boolean hasPlayerFoundAdvancement(@NotNull UUID playerUUID, @NotNull KatzeAdvancement advancement) {
        if (AllItemsSettings.isTeamMode()) {
            return isAdvancementFound(advancement);
        }
        return playerFoundAdvancements.getOrDefault(playerUUID, Collections.emptySet()).contains(advancement);
    }

    public Set<Material> getItemsFoundByPlayer(@NotNull UUID playerUUID) {
        if (AllItemsSettings.isTeamMode()) {
            return Set.copyOf(teamFoundItems.keySet());
        }
        return Set.copyOf(playerFoundItems.getOrDefault(playerUUID, Collections.emptySet()));
    }

    public Set<BiomeSimplified> getBiomesFoundByPlayer(@NotNull UUID playerUUID) {
        if (AllItemsSettings.isTeamMode()) {
            return Set.copyOf(teamFoundBiomes.keySet());
        }
        return Set.copyOf(playerFoundBiomes.getOrDefault(playerUUID, Collections.emptySet()));
    }

    public Set<EntityType> getMobsFoundByPlayer(@NotNull UUID playerUUID) {
        if (AllItemsSettings.isTeamMode()) {
            return Set.copyOf(teamFoundMobs.keySet());
        }
        return Set.copyOf(playerFoundMobs.getOrDefault(playerUUID, Collections.emptySet()));
    }

    public Set<StructureSimplified> getStructuresFoundByPlayer(@NotNull UUID playerUUID) {
        if (AllItemsSettings.isTeamMode()) {
            return Set.copyOf(teamFoundStructures.keySet());
        }
        return Set.copyOf(playerFoundStructures.getOrDefault(playerUUID, Collections.emptySet()));
    }

    public Set<KatzeAdvancement> getAdvancementsFoundByPlayer(@NotNull UUID playerUUID) {
        if (AllItemsSettings.isTeamMode()) {
            return Set.copyOf(teamFoundAdvancements.keySet());
        }
        return Set.copyOf(playerFoundAdvancements.getOrDefault(playerUUID, Collections.emptySet()));
    }

    public int getTotalAvailableItems() {
        return (int) Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(material -> !material.name().contains("LEGACY"))
                .count();
    }

    public int getTotalAvailableBiomes() {
        return BiomeSimplified.values().length;
    }

    public int getTotalAvailableMobs() {
        return getAvailableMobs().size();
    }

    public int getTotalAvailableStructures() {
        return StructureSimplified.values().length;
    }

    public int getTotalAvailableAdvancements() {
        return KatzeAdvancement.values().length;
    }

    public double getProgressPercentage(AllItemsObjectiveType type, @Nullable UUID playerUUID) {
        int found = getFoundCount(type, playerUUID);
        int total = getTotalCount(type);
        return total > 0 ? (double) found / total * 100.0 : 0.0;
    }

    public int getFoundCount(AllItemsObjectiveType type, @Nullable UUID playerUUID) {
        boolean useTeam = AllItemsSettings.isTeamMode() || playerUUID == null;
        return switch (type) {
            case ITEM -> useTeam ? teamFoundItems.size() : getItemsFoundByPlayer(playerUUID).size();
            case BIOME -> useTeam ? teamFoundBiomes.size() : getBiomesFoundByPlayer(playerUUID).size();
            case MOB -> useTeam ? teamFoundMobs.size() : getMobsFoundByPlayer(playerUUID).size();
            case STRUCTURE -> useTeam ? teamFoundStructures.size() : getStructuresFoundByPlayer(playerUUID).size();
            case ADVANCEMENT -> useTeam ? teamFoundAdvancements.size() : getAdvancementsFoundByPlayer(playerUUID).size();
        };
    }

    private int getTotalCount(AllItemsObjectiveType type) {
        return switch (type) {
            case ITEM -> getTotalAvailableItems();
            case BIOME -> getTotalAvailableBiomes();
            case MOB -> getTotalAvailableMobs();
            case STRUCTURE -> getTotalAvailableStructures();
            case ADVANCEMENT -> getTotalAvailableAdvancements();
        };
    }

    public List<Material> getMissingItems(@NotNull UUID playerUUID) {
        Set<Material> found = getItemsFoundByPlayer(playerUUID);
        return Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(material -> !material.name().contains("LEGACY"))
                .filter(material -> !found.contains(material))
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    public List<BiomeSimplified> getMissingBiomes(@NotNull UUID playerUUID) {
        Set<BiomeSimplified> found = getBiomesFoundByPlayer(playerUUID);
        return Arrays.stream(BiomeSimplified.values())
                .filter(biome -> !found.contains(biome))
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    public List<EntityType> getMissingMobs(@NotNull UUID playerUUID) {
        Set<EntityType> found = getMobsFoundByPlayer(playerUUID);
        return getAvailableMobs().stream()
                .filter(type -> !found.contains(type))
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    public List<StructureSimplified> getMissingStructures(@NotNull UUID playerUUID) {
        Set<StructureSimplified> found = getStructuresFoundByPlayer(playerUUID);
        return Arrays.stream(StructureSimplified.values())
                .filter(structure -> !found.contains(structure))
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    public List<KatzeAdvancement> getMissingAdvancements(@NotNull UUID playerUUID) {
        Set<KatzeAdvancement> found = getAdvancementsFoundByPlayer(playerUUID);
        return Arrays.stream(KatzeAdvancement.values())
                .filter(advancement -> !found.contains(advancement))
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    public List<FoundObjective> getRecentFinds(int limit) {
        synchronized (recentFinds) {
            int fromIndex = Math.max(0, recentFinds.size() - limit);
            return new ArrayList<>(recentFinds.subList(fromIndex, recentFinds.size()));
        }
    }

    public @Nullable AllItemsObjective getCurrentObjective(@NotNull Player player) {
        if (!AllItemsSettings.isChainModeEnabled()) {
            return null;
        }
        ChainState state = getChainState(player);
        return state == null ? null : state.getCurrent();
    }

    public void resetChains() {
        teamChain = null;
        playerChains.clear();
    }

    public void saveToFile() {
        SaveData data = new SaveData();
        data.gameMode = AllItemsSettings.getGameMode().name();
        data.teamItems = serializeFoundItems(teamFoundItems);
        data.teamBiomes = serializeFoundBiomes(teamFoundBiomes);
        data.teamMobs = serializeFoundMobs(teamFoundMobs);
        data.teamStructures = serializeFoundStructures(teamFoundStructures);
        data.teamAdvancements = serializeFoundAdvancements(teamFoundAdvancements);
        data.playerProgress = serializePlayerProgress();
        data.teamChain = teamChain == null ? null : ChainJson.from(teamChain);
        playerChains.forEach((uuid, chain) -> data.playerChains.put(uuid.toString(), ChainJson.from(chain)));
        synchronized (recentFinds) {
            data.recentFinds = recentFinds.stream().map(RecentFindJson::of).toList();
        }

        WorldStorage.save(DATA_FILE_NAME, data);
    }

    public void loadFromFile() {
        SaveData data = WorldStorage.load(DATA_FILE_NAME, SaveData.class);
        if (data == null) {
            return;
        }

        try {
            if ("INDIVIDUAL".equalsIgnoreCase(data.gameMode)) {
                AllItemsSettings.setGameMode(AllItemsSettings.GameMode.INDIVIDUAL);
            } else {
                AllItemsSettings.setGameMode(AllItemsSettings.GameMode.TEAM);
            }

            clearAllProgress();
            if (data.teamItems != null) {
                data.teamItems.forEach((key, entry) -> {
                    Material material = Material.matchMaterial(key);
                    if (material != null) {
                        teamFoundItems.put(material, entry.toFoundEntry());
                    }
                });
            }
            if (data.teamBiomes != null) {
                data.teamBiomes.forEach((key, entry) -> {
                    try {
                        teamFoundBiomes.put(BiomeSimplified.valueOf(key), entry.toFoundEntry());
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.teamMobs != null) {
                data.teamMobs.forEach((key, entry) -> {
                    try {
                        teamFoundMobs.put(EntityType.valueOf(key), entry.toFoundEntry());
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.teamStructures != null) {
                data.teamStructures.forEach((key, entry) -> {
                    try {
                        teamFoundStructures.put(StructureSimplified.valueOf(key), entry.toFoundEntry());
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.teamAdvancements != null) {
                data.teamAdvancements.forEach((key, entry) -> {
                    try {
                        teamFoundAdvancements.put(KatzeAdvancement.valueOf(key), entry.toFoundEntry());
                    } catch (IllegalArgumentException ignored) {}
                });
            }

            if (data.playerProgress != null) {
                data.playerProgress.forEach((uuid, progress) -> {
                    try {
                        UUID playerUUID = UUID.fromString(uuid);
                        playerFoundItems.put(playerUUID, progress.readItems());
                        playerFoundBiomes.put(playerUUID, progress.readBiomes());
                        playerFoundMobs.put(playerUUID, progress.readMobs());
                        playerFoundStructures.put(playerUUID, progress.readStructures());
                        playerFoundAdvancements.put(playerUUID, progress.readAdvancements());
                    } catch (IllegalArgumentException ignored) {}
                });
            }

            if (data.teamChain != null) {
                teamChain = data.teamChain.toChainState();
            }
            if (data.playerChains != null) {
                data.playerChains.forEach((uuid, chain) -> {
                    try {
                        ChainState state = chain.toChainState();
                        if (state != null) {
                            playerChains.put(UUID.fromString(uuid), state);
                        }
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.recentFinds != null) {
                for (RecentFindJson find : data.recentFinds) {
                    FoundObjective objective = find.toFoundObjective();
                    if (objective != null) {
                        recentFinds.add(objective);
                    }
                }
            }
        } catch (RuntimeException e) {
            KatzeGames.get().getLogger().warning("Failed to restore All Items data: " + e.getMessage());
        }
    }

    public void clearAllProgress() {
        teamFoundItems.clear();
        teamFoundBiomes.clear();
        teamFoundMobs.clear();
        teamFoundStructures.clear();
        teamFoundAdvancements.clear();
        playerFoundItems.clear();
        playerFoundBiomes.clear();
        playerFoundMobs.clear();
        playerFoundStructures.clear();
        playerFoundAdvancements.clear();
        recentFinds.clear();
        resetChains();
    }

    private FoundEntry buildEntry(Player player) {
        return new FoundEntry(player.getName(), player.getUniqueId(), LocalDateTime.now());
    }

    private void addRecentFind(AllItemsObjectiveType type, String key, String displayName, FoundEntry entry) {
        recentFinds.add(new FoundObjective(type, key, displayName, entry));
        if (recentFinds.size() > 200) {
            recentFinds.removeFirst();
        }
    }

    private void advanceChainIfNeeded(Player player, AllItemsObjectiveType type, String key) {
        if (!AllItemsSettings.isChainModeEnabled()) {
            return;
        }
        ChainState state = getChainState(player);
        if (state == null) {
            return;
        }
        if (state.getCurrent() != null && state.getCurrent().matches(type, key)) {
            state.advance();
        }
    }

    private @Nullable ChainState getChainState(Player player) {
        if (AllItemsSettings.isTeamMode()) {
            if (teamChain == null || teamChain.isExhausted()) {
                teamChain = buildChain(null);
            }
            return teamChain;
        }
        return playerChains.computeIfAbsent(player.getUniqueId(), this::buildChain);
    }

    private ChainState buildChain(@Nullable UUID playerUUID) {
        List<AllItemsObjective> pool = buildObjectivePool(playerUUID);
        Collections.shuffle(pool, new Random());
        return new ChainState(pool);
    }

    private List<AllItemsObjective> buildObjectivePool(@Nullable UUID playerUUID) {
        List<AllItemsObjective> pool = new ArrayList<>();

        if (AllItemsSettings.isItemsEnabled()) {
            for (Material material : getMissingItems(resolveMissingPlayer(playerUUID))) {
                pool.add(new AllItemsObjective(AllItemsObjectiveType.ITEM, material.name(), formatMaterialName(material)));
            }
        }
        if (AllItemsSettings.isBiomesEnabled()) {
            for (BiomeSimplified biome : getMissingBiomes(resolveMissingPlayer(playerUUID))) {
                pool.add(new AllItemsObjective(AllItemsObjectiveType.BIOME, biome.name(), biome.getName()));
            }
        }
        if (AllItemsSettings.isMobsEnabled()) {
            for (EntityType type : getMissingMobs(resolveMissingPlayer(playerUUID))) {
                pool.add(new AllItemsObjective(AllItemsObjectiveType.MOB, type.name(), formatMaterialName(type.name())));
            }
        }
        if (AllItemsSettings.isStructuresEnabled()) {
            for (StructureSimplified structure : getMissingStructures(resolveMissingPlayer(playerUUID))) {
                pool.add(new AllItemsObjective(AllItemsObjectiveType.STRUCTURE, structure.name(), structure.getName()));
            }
        }
        if (AllItemsSettings.isAdvancementsEnabled()) {
            for (KatzeAdvancement advancement : getMissingAdvancements(resolveMissingPlayer(playerUUID))) {
                pool.add(new AllItemsObjective(AllItemsObjectiveType.ADVANCEMENT, advancement.name(), advancement.getName()));
            }
        }
        return pool;
    }

    private UUID resolveMissingPlayer(@Nullable UUID playerUUID) {
        return playerUUID != null ? playerUUID : new UUID(0L, 0L);
    }

    private List<EntityType> getAvailableMobs() {
        return Arrays.stream(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(EntityType::isSpawnable)
                .filter(type -> type != EntityType.PLAYER)
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    private String formatMaterialName(Material material) {
        return StringUtility.formatName(material.name());
    }

    private String formatMaterialName(String raw) {
        return StringUtility.formatName(raw);
    }

    private Map<String, FoundEntryJson> serializeFoundItems(Map<Material, FoundEntry> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> FoundEntryJson.from(entry.getValue())));
    }

    private Map<String, FoundEntryJson> serializeFoundBiomes(Map<BiomeSimplified, FoundEntry> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> FoundEntryJson.from(entry.getValue())));
    }

    private Map<String, FoundEntryJson> serializeFoundMobs(Map<EntityType, FoundEntry> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> FoundEntryJson.from(entry.getValue())));
    }

    private Map<String, FoundEntryJson> serializeFoundStructures(Map<StructureSimplified, FoundEntry> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> FoundEntryJson.from(entry.getValue())));
    }

    private Map<String, FoundEntryJson> serializeFoundAdvancements(Map<KatzeAdvancement, FoundEntry> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> FoundEntryJson.from(entry.getValue())));
    }

    private Map<String, PlayerProgress> serializePlayerProgress() {
        Map<String, PlayerProgress> progress = new HashMap<>();
        Set<UUID> allPlayers = ConcurrentHashMap.newKeySet();
        allPlayers.addAll(playerFoundItems.keySet());
        allPlayers.addAll(playerFoundBiomes.keySet());
        allPlayers.addAll(playerFoundMobs.keySet());
        allPlayers.addAll(playerFoundStructures.keySet());
        allPlayers.addAll(playerFoundAdvancements.keySet());

        for (UUID uuid : allPlayers) {
            PlayerProgress player = new PlayerProgress();
            player.items = playerFoundItems.getOrDefault(uuid, Set.of()).stream().map(Enum::name).toList();
            player.biomes = playerFoundBiomes.getOrDefault(uuid, Set.of()).stream().map(Enum::name).toList();
            player.mobs = playerFoundMobs.getOrDefault(uuid, Set.of()).stream().map(Enum::name).toList();
            player.structures = playerFoundStructures.getOrDefault(uuid, Set.of()).stream().map(Enum::name).toList();
            player.advancements = playerFoundAdvancements.getOrDefault(uuid, Set.of()).stream().map(Enum::name).toList();
            progress.put(uuid.toString(), player);
        }
        return progress;
    }

    private static @Nullable AllItemsObjectiveType parseObjectiveType(@Nullable String name) {
        if (name == null) {
            return null;
        }
        try {
            return AllItemsObjectiveType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static class ChainState {
        private final List<AllItemsObjective> objectives;
        private int index;

        ChainState(List<AllItemsObjective> objectives) {
            this(objectives, 0);
        }

        ChainState(List<AllItemsObjective> objectives, int index) {
            this.objectives = objectives;
            this.index = index;
        }

        @Nullable AllItemsObjective getCurrent() {
            if (index >= objectives.size()) {
                return null;
            }
            return objectives.get(index);
        }

        void advance() {
            index++;
        }

        boolean isExhausted() {
            return objectives.isEmpty() || index >= objectives.size();
        }
    }

    private static class SaveData {
        String gameMode;
        Map<String, FoundEntryJson> teamItems;
        Map<String, FoundEntryJson> teamBiomes;
        Map<String, FoundEntryJson> teamMobs;
        Map<String, FoundEntryJson> teamStructures;
        Map<String, FoundEntryJson> teamAdvancements;
        Map<String, PlayerProgress> playerProgress;
        ChainJson teamChain;
        Map<String, ChainJson> playerChains = new HashMap<>();
        List<RecentFindJson> recentFinds = new ArrayList<>();
    }

    private static class ChainJson {
        List<ObjectiveJson> objectives = new ArrayList<>();
        int index;

        static ChainJson from(ChainState state) {
            ChainJson json = new ChainJson();
            json.objectives = state.objectives.stream().map(ObjectiveJson::from).toList();
            json.index = state.index;
            return json;
        }

        @Nullable ChainState toChainState() {
            if (objectives == null || index < 0 || index > objectives.size()) {
                return null;
            }
            List<AllItemsObjective> restored = new ArrayList<>();
            for (ObjectiveJson objective : objectives) {
                AllItemsObjective decoded = objective.toObjective();
                if (decoded == null) {
                    return null;
                }
                restored.add(decoded);
            }
            return new ChainState(restored, index);
        }
    }

    private static class ObjectiveJson {
        String type;
        String key;
        String displayName;

        static ObjectiveJson from(AllItemsObjective objective) {
            ObjectiveJson json = new ObjectiveJson();
            json.type = objective.type().name();
            json.key = objective.key();
            json.displayName = objective.displayName();
            return json;
        }

        @Nullable AllItemsObjective toObjective() {
            AllItemsObjectiveType objectiveType = parseObjectiveType(type);
            if (objectiveType == null || key == null) {
                return null;
            }
            return new AllItemsObjective(objectiveType, key, Objects.requireNonNullElse(displayName, key));
        }
    }

    private static class PlayerProgress {
        List<String> items = List.of();
        List<String> biomes = List.of();
        List<String> mobs = List.of();
        List<String> structures = List.of();
        List<String> advancements = List.of();

        Set<Material> readItems() {
            return items.stream()
                    .map(Material::matchMaterial)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        }

        Set<BiomeSimplified> readBiomes() {
            return biomes.stream()
                    .map(name -> {
                        try {
                            return BiomeSimplified.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        }

        Set<EntityType> readMobs() {
            return mobs.stream()
                    .map(name -> {
                        try {
                            return EntityType.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        }

        Set<StructureSimplified> readStructures() {
            return structures.stream()
                    .map(name -> {
                        try {
                            return StructureSimplified.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        }

        Set<KatzeAdvancement> readAdvancements() {
            return advancements.stream()
                    .map(name -> {
                        try {
                            return KatzeAdvancement.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        }
    }

    private static class FoundEntryJson {
        String playerName;
        String playerUUID;
        String foundTime;

        static FoundEntryJson from(FoundEntry entry) {
            FoundEntryJson json = new FoundEntryJson();
            json.playerName = entry.playerName();
            json.playerUUID = entry.playerUUID().toString();
            json.foundTime = entry.getFormattedTime();
            return json;
        }

        FoundEntry toFoundEntry() {
            LocalDateTime time;
            try {
                time = LocalDateTime.parse(foundTime, TIMESTAMP_FORMAT);
            } catch (Exception e) {
                time = LocalDateTime.now();
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(playerUUID);
            } catch (IllegalArgumentException e) {
                uuid = new UUID(0L, 0L);
            }
            return new FoundEntry(playerName, uuid, time);
        }
    }

    private static class RecentFindJson extends FoundEntryJson {
        String type;
        String key;
        String displayName;

        static RecentFindJson of(FoundObjective find) {
            RecentFindJson json = new RecentFindJson();
            json.type = find.type().name();
            json.key = find.key();
            json.displayName = find.displayName();
            json.playerName = find.entry().playerName();
            json.playerUUID = find.entry().playerUUID().toString();
            json.foundTime = find.entry().getFormattedTime();
            return json;
        }

        @Nullable FoundObjective toFoundObjective() {
            AllItemsObjectiveType objectiveType = parseObjectiveType(type);
            if (objectiveType == null || key == null) {
                return null;
            }
            return new FoundObjective(objectiveType, key, Objects.requireNonNullElse(displayName, key), toFoundEntry());
        }
    }
}

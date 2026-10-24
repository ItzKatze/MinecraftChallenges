package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.DamageTypeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.KatzeDamageType;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.MobsSimplified;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.forcebattle.exclusion.ObjectiveFilter;
import gg.itzkatze.katze.player.KatzePlayer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ForceBattleObjectiveManager {

    private static final int MIN_HEIGHT = -63;
    private static final int MAX_HEIGHT = 320;

    private static ForceBattleObjectiveManager instance;

    public static ForceBattleObjectiveManager getInstance() {
        if (instance == null) {
            instance = new ForceBattleObjectiveManager();
        }
        return instance;
    }

    private final List<ObjectiveSeed> chainList = new ArrayList<>();
    private final List<ObjectiveSeed> teamChainList = new ArrayList<>();

    private ForceBattleObjectiveManager() {
    }

    public ForceBattleObjective getNewObjective(ForceBattleTeam team) {
        if (SettingsManager.isEnabled(ForceBattleSettings.CHAIN_MODE)) {
            return chainObjective(getTeamChainList(), team.getChainProgress());
        }

        List<KatzePlayer> players = team.getPlayers();
        if (players.isEmpty()) return null;

        ForceBattlePlayer player = (ForceBattlePlayer) players.get(ThreadLocalRandom.current().nextInt(players.size()));
        ForceBattleType battleType = randomBattleType(PlayerTeamBothState.TEAM);
        if (battleType == null) return null;

        return generateObjective(battleType, player, team);
    }

    public ForceBattleObjective getNewObjective(ForceBattlePlayer player) {
        if (SettingsManager.isEnabled(ForceBattleSettings.CHAIN_MODE)) {
            return chainObjective(getChainList(), player.getChainProgress());
        }

        ForceBattleType battleType = randomBattleType(PlayerTeamBothState.PLAYER);
        if (battleType == null) return null;

        return generateObjective(battleType, player, null);
    }

    private @Nullable ForceBattleType randomBattleType(@NotNull PlayerTeamBothState audience) {
        List<ForceBattleType> availableBattleTypes = Arrays.stream(ForceBattleType.values()).filter(type -> isEnabledFor(type, audience)).toList();
        if (availableBattleTypes.isEmpty()) return null;

        return availableBattleTypes.get(ThreadLocalRandom.current().nextInt(availableBattleTypes.size()));
    }

    private static boolean isEnabledFor(@NotNull ForceBattleType type, @NotNull PlayerTeamBothState audience) {
        SettingStateType state = type.getSettingState();
        return state == audience || state == PlayerTeamBothState.BOTH;
    }

    private ForceBattleObjective generateObjective(ForceBattleType battleType, ForceBattlePlayer player, ForceBattleTeam team) {
        List<ObjectiveSeed> allPossible = getCandidates(battleType, player, ObjectiveFilter.current(), isSimplified());
        if (allPossible.isEmpty()) return null;

        if (SettingsManager.isEnabled(GlobalSetting.NO_DUPLICATE_OBJECTIVES)) {
            Set<String> finished = new HashSet<>();
            if (team == null || !player.isInTeam()) {
                player.getSkippedObjectives().forEach(obj -> finished.add(obj.getObjectiveString()));
                ForceBattleObjective.finishedBy(player).forEach(obj -> finished.add(obj.getObjectiveString()));
            } else {
                for (KatzePlayer teammate : team.getPlayers()) {
                    ForceBattlePlayer teammatePlayer = (ForceBattlePlayer) teammate;
                    teammatePlayer.getSkippedObjectives().forEach(obj -> finished.add(obj.getObjectiveString()));
                    ForceBattleObjective.finishedBy(teammate).forEach(obj -> finished.add(obj.getObjectiveString()));
                }
            }
            allPossible.removeIf(seed -> finished.contains(seed.getKey()));
        }

        if (allPossible.isEmpty()) {
            player.sendMessage(Component.text("No objective available for BattleType " + battleType.name() + ". Possible all completed already?")
                    .color(NamedTextColor.RED));
            if (team != null && SettingsManager.isEnabled(GlobalSetting.EXTRA_TEAM_OBJECTIVE)) {
                return getNewObjective(team);
            } else {
                return getNewObjective(player);
            }
        }

        return allPossible.get(ThreadLocalRandom.current().nextInt(allPossible.size())).toObjective();
    }

    private static boolean isSimplified() {
        return SettingsManager.isEnabled(GlobalSetting.SIMPLIFIED_OBJECTIVES);
    }

    @NotNull List<ObjectiveSeed> getCandidates(@NotNull ForceBattleType type, @Nullable KatzePlayer player, @NotNull ObjectiveFilter filter, boolean simplified) {
        Stream<?> values = switch (type) {
            case FORCE_ITEM -> Arrays.stream(Material.values()).filter(material -> !material.isLegacy() && material.isItem());
            case FORCE_MOB -> simplified
                    ? Arrays.stream(MobsSimplified.values())
                    : Arrays.stream(EntityType.values()).filter(entityType -> entityType.isAlive() && entityType.isSpawnable());
            case FORCE_BIOME -> simplified ? Arrays.stream(BiomeSimplified.values()) : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream();
            case FORCE_ADVANCEMENT -> Arrays.stream(KatzeAdvancement.values());
            case FORCE_HEIGHT -> IntStream.rangeClosed(MIN_HEIGHT, MAX_HEIGHT).boxed();
            case FORCE_COORDS -> player == null ? Stream.empty() : Stream.of(getRandomLocation(player));
            case FORCE_STRUCTURE -> simplified ? Arrays.stream(StructureSimplified.values()) : Arrays.stream(KatzeStructure.values());
            case FORCE_DEATH -> simplified ? Arrays.stream(DamageTypeSimplified.values()) : Arrays.stream(KatzeDamageType.values());
        };
        return values.filter(value -> !filter.isExcluded(value))
                .map(value -> new ObjectiveSeed(type, value))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public @NotNull List<ObjectiveSeed> exportChain() {
        return List.copyOf(chainList);
    }

    public @NotNull List<ObjectiveSeed> exportTeamChain() {
        return List.copyOf(teamChainList);
    }

    public void importChains(@NotNull List<ObjectiveSeed> chain, @NotNull List<ObjectiveSeed> teamChain) {
        chainList.clear();
        chainList.addAll(chain);
        teamChainList.clear();
        teamChainList.addAll(teamChain);
    }

    public void clearChains() {
        chainList.clear();
        teamChainList.clear();
    }

    private List<ObjectiveSeed> getChainList() {
        if (chainList.isEmpty()) {
            chainList.addAll(buildChain(PlayerTeamBothState.PLAYER, ObjectiveFilter.current(), isSimplified()));
        }
        return chainList;
    }

    private List<ObjectiveSeed> getTeamChainList() {
        if (teamChainList.isEmpty()) {
            teamChainList.addAll(buildChain(PlayerTeamBothState.TEAM, ObjectiveFilter.current(), isSimplified()));
        }
        return teamChainList;
    }

    @NotNull List<ObjectiveSeed> buildChain(@NotNull PlayerTeamBothState audience, @NotNull ObjectiveFilter filter, boolean simplified) {
        List<ObjectiveSeed> chain = new ArrayList<>();
        for (ForceBattleType type : ForceBattleType.values()) {
            if (type != ForceBattleType.FORCE_COORDS && isEnabledFor(type, audience)) {
                chain.addAll(getCandidates(type, null, filter, simplified));
            }
        }
        Collections.shuffle(chain);
        return chain;
    }

    static @Nullable ForceBattleObjective chainObjective(@NotNull List<ObjectiveSeed> chain, int progress) {
        if (chain.isEmpty()) return null;

        ObjectiveSeed seed = progress >= 0 && progress < chain.size() ? chain.get(progress) : chain.getFirst();
        return seed.toObjective();
    }

    public Location getRandomLocation(KatzePlayer katzePlayer) {
        org.bukkit.entity.Player player = katzePlayer.getPlayer();
        if (player == null) return new Location(Bukkit.getWorld("world"), 0, 0, 0);

        int maxDistance = 2000;

        int offsetX = ThreadLocalRandom.current().nextInt(-maxDistance, maxDistance + 1);
        int offsetZ = ThreadLocalRandom.current().nextInt(-maxDistance, maxDistance + 1);

        Location base = player.getLocation();

        int x = (int) base.getX() + offsetX;
        int z = (int) base.getZ() + offsetZ;

        int y = player.getWorld().getHighestBlockYAt(x, z);

        return new Location(player.getWorld(), x, y, z);
    }

    @Getter
    public static final class ObjectiveSeed {
        private final ForceBattleType type;
        private final Object value;
        private final String key;

        public ObjectiveSeed(ForceBattleType type, Object value) {
            this.type = type;
            this.value = value;
            this.key = ForceBattleObjective.buildObjectiveString(type, value);
        }

        public ForceBattleObjective toObjective() {
            return new ForceBattleObjective(type, value);
        }
    }
}

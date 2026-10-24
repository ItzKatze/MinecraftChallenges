package gg.itzkatze.katze.team;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.event.PlayerTeamJoinEvent;
import gg.itzkatze.katze.event.PlayerTeamLeaveEvent;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class KatzeTeam {

    private static final Logger logger = LoggerFactory.getLogger(KatzeTeam.class.getSimpleName());
    public static final List<KatzeTeam> teams = new ArrayList<>();
    private static final Map<ChallengeType, TeamAdapterFactory> factories = new HashMap<>();

    @Getter
    protected final List<KatzePlayer> players;
    protected final int ID;
    protected boolean privateTeam = false;
    @Getter
    protected JoinRequest lastJoinRequest = null;

    public KatzeTeam(List<KatzePlayer> players) {
        this(players, nextId(), true);
    }

    protected KatzeTeam(List<KatzePlayer> players, int id, boolean register) {
        this.players = new ArrayList<>(players);
        this.ID = id;
        if (register) {
            teams.add(this);
        }
    }

    /**
     * Registers a factory for creating challenge-specific team wrappers.
     */
    public static void registerFactory(@NotNull ChallengeType type, @NotNull TeamAdapterFactory factory) {
        factories.put(type, factory);
    }

    /**
     * Unregisters the factory for the given challenge type.
     */
    public static void unregisterFactory(@NotNull ChallengeType type) {
        factories.remove(type);
    }

    /**
     * Creates a team using a registered challenge factory if available.
     */
    public static @NotNull KatzeTeam create(@NotNull List<KatzePlayer> players) {
        return create(players, nextId());
    }

    public static @NotNull KatzeTeam restore(int id, boolean privateTeam, @NotNull List<KatzePlayer> players) {
        KatzeTeam team = create(players, id);
        team.privateTeam = privateTeam;
        return team;
    }

    private static @NotNull KatzeTeam create(@NotNull List<KatzePlayer> players, int id) {
        for (Challenge challenge : ChallengeManager.getActiveChallenges()) {
            TeamAdapterFactory factory = factories.get(challenge.getType());
            if (factory != null) {
                return factory.adapt(players, id, true);
            }
        }
        return new KatzeTeam(players, id, true);
    }

    /**
     * Swaps a player wrapper for its replacement in every team, e.g. when wrappers are upgraded.
     */
    public static void replacePlayer(@NotNull KatzePlayer old, @NotNull KatzePlayer replacement) {
        for (KatzeTeam team : teams) {
            Collections.replaceAll(team.players, old, replacement);
            if (team.lastJoinRequest != null && team.lastJoinRequest.katzePlayer() == old) {
                team.lastJoinRequest = new JoinRequest(replacement, team);
            }
        }
    }

    /**
     * Upgrades existing teams to a challenge-specific implementation when a factory is registered.
     */
    public static void upgrade(@NotNull ChallengeType type) {
        TeamAdapterFactory factory = factories.get(type);
        if (factory == null) return;

        List<KatzeTeam> current = new ArrayList<>(teams);
        teams.clear();

        for (KatzeTeam team : current) {
            KatzeTeam upgraded = factory.adapt(new ArrayList<>(team.getPlayers()), team.getId(), true);
            upgraded.privateTeam = team.privateTeam;
            if (team.lastJoinRequest != null) {
                upgraded.lastJoinRequest = new JoinRequest(team.lastJoinRequest.katzePlayer(), upgraded);
            }
        }
    }

    /**
     * Downgrades existing teams to base teams when a challenge is disabled.
     */
    public static void downgrade(@NotNull ChallengeType type) {
        if (!factories.containsKey(type)) return;

        List<KatzeTeam> current = new ArrayList<>(teams);
        teams.clear();

        for (KatzeTeam team : current) {
            KatzeTeam base = new KatzeTeam(new ArrayList<>(team.getPlayers()), team.getId(), true);
            base.privateTeam = team.privateTeam;
            if (team.lastJoinRequest != null) {
                base.lastJoinRequest = new JoinRequest(team.lastJoinRequest.katzePlayer(), base);
            }
        }
    }

    private static int nextId() {
        List<Integer> usedIdList = new ArrayList<>();
        for (KatzeTeam team : teams) {
            usedIdList.add(team.ID);
        }

        int id = 0;
        while (usedIdList.contains(id)) {
            id++;
        }
        return id;
    }

    public static Optional<KatzeTeam> ofId(int id) {
        for (KatzeTeam team : teams) {
            if (team.getId() == id) {
                return Optional.of(team);
            }
        }
        return Optional.empty();
    }

    public void sendJoinRequest(KatzePlayer katzePlayer) {
        if (!isPrivate()) {
            katzePlayer.addToTeam(this);
            return;
        }
        this.lastJoinRequest = new JoinRequest(katzePlayer, this);
        players.forEach(teamPlayer ->
                teamPlayer.sendMessage(Component.text(katzePlayer.getName(), NamedTextColor.BLUE)
                        .append(Component.text(" has requested to join your team. /team accept|reject.", NamedTextColor.GRAY)))
        );
    }

    public void finishLastJoinRequest(boolean accept) {
        if (accept) {
            lastJoinRequest.accept();
        } else {
            lastJoinRequest.reject();
        }
        this.lastJoinRequest = null;
    }

    public boolean isPrivate() {
        return privateTeam;
    }

    public void setPrivate(boolean privateTeam) {
        this.privateTeam = privateTeam;
    }

    public void addPlayer(KatzePlayer katzePlayer) {
        PlayerTeamJoinEvent teamJoinEvent = new PlayerTeamJoinEvent(this, katzePlayer);
        Bukkit.getPluginManager().callEvent(teamJoinEvent);
        if (teamJoinEvent.isCancelled()) {
            logger.info("PlayerTeamJoinEvent has been denied by an external plugin.");
            return;
        }

        players.forEach(teamPlayer ->
                teamPlayer.sendMessage(Component.text(katzePlayer.getName(), NamedTextColor.BLUE)
                        .append(Component.text(" has joined your team.", NamedTextColor.GRAY))));
        players.add(katzePlayer);
        katzePlayer.sendMessage(Component.text("You have joined team " + this.getId() + ".", NamedTextColor.GREEN));
    }

    public void removePlayer(KatzePlayer katzePlayer) {
        PlayerTeamLeaveEvent teamLeaveEvent = new PlayerTeamLeaveEvent(this, katzePlayer);
        Bukkit.getPluginManager().callEvent(teamLeaveEvent);
        if (teamLeaveEvent.isCancelled()) {
            logger.info("PlayerTeamLeaveEvent has been denied by an external plugin.");
            return;
        }

        players.remove(katzePlayer);
        players.forEach(teamPlayer ->
                teamPlayer.sendMessage(Component.text(katzePlayer.getName(), NamedTextColor.BLUE)
                        .append(Component.text(" has left your team.", NamedTextColor.GRAY))));
        if (players.isEmpty()) {
            this.delete();
        }
    }

    public void delete() {
        teams.remove(this);
    }

    public static void deleteAll() {
        for (KatzeTeam team : new ArrayList<>(teams)) {
            team.delete();
        }
    }

    public int getPoints() {
        int teamPoints = 0;

        for (KatzePlayer teamPlayer : players) {
            teamPoints += teamPlayer.getPoints();
        }

        return teamPoints;
    }

    public int getId() {
        return ID;
    }

    public static Map<Integer, KatzeTeam> getPlaces() {
        Map<Integer, KatzeTeam> places = new HashMap<>();

        final Map<KatzeTeam, Integer> TEAM_POINTS_MAP = new HashMap<>();
        for (KatzeTeam team : teams) {
            TEAM_POINTS_MAP.put(team, team.getPoints());
        }

        List<Map.Entry<KatzeTeam, Integer>> sortedTeams = TEAM_POINTS_MAP.entrySet()
                .stream()
                .sorted(Map.Entry.<KatzeTeam, Integer>comparingByValue().reversed())
                .toList();

        int place = 1;
        for (Map.Entry<KatzeTeam, Integer> entry : sortedTeams) {
            places.put(place++, entry.getKey());
        }

        return places;
    }

    public static int getPlace(KatzeTeam team) {
        List<KatzeTeam> sortedTeams = new ArrayList<>(KatzeTeam.teams);
        sortedTeams.sort(Comparator.comparingInt(KatzeTeam::getPoints).reversed());

        int place = 1;
        int previousPoints = -1;

        for (int i = 0; i < sortedTeams.size(); i++) {
            KatzeTeam current = sortedTeams.get(i);

            if (current.getPoints() != previousPoints) {
                place = i + 1;
                previousPoints = current.getPoints();
            }

            if (current.equals(team)) {
                return place;
            }
        }

        return -1;
    }

    /**
     * Gets the current objective. Default implementation returns null.
     * Challenges should override this in their team implementations.
     */
    public Object getObjective() {
        return null;
    }

    /**
     * Called when an objective is completed. Default implementation does nothing.
     * Challenges should override this in their team implementations.
     */
    public void updateObjective(KatzePlayer finisher, boolean finishLast, boolean hasBeenSkipped) {
        // Default implementation - does nothing
        // Challenge-specific team classes should override this
    }


    public record JoinRequest(KatzePlayer katzePlayer, KatzeTeam team) {

        public void accept() {
                katzePlayer.addToTeam(team);
            }

            public void reject() {
                katzePlayer.sendMessage(Component.text("Your join request has been rejected.", NamedTextColor.GRAY));
            }

        }

}

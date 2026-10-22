package gg.itzkatze.mcc.lobby.islands;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.lobby.LobbyFeature;
import gg.itzkatze.mcc.lobby.LobbyIsland;
import gg.itzkatze.mcc.lobby.cosmetics.Leaderboard;
import gg.itzkatze.mcc.lobby.cosmetics.cases.CaseSpot;
import gg.itzkatze.mcc.lobby.jumpandrun.JumpAndRun;
import gg.itzkatze.mcc.lobby.npc.NpcSpot;
import gg.itzkatze.mcc.lobby.npc.impl.CosmeticShopNpc;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InselMitHausIsland extends LobbyIsland {

    @Override
    public @NotNull String getId() {
        return "inselmithaus";
    }

    @Override
    public @NotNull String getStructureName() {
        return "lobby/inselmithaus";
    }

    @Override
    public @NotNull Vector getSpawnPosition() {
        return new Vector(37, 9, 23);
    }

    @Override
    public @NotNull List<LobbyFeature> createFeatures() {
        return List.of(
                new JumpAndRun(new Vector(20, 13, 38), MinecraftChallenges.get()),
                new NpcSpot(new CosmeticShopNpc(), new Vector(7.5, 9, 22.5), -65f),
                new CaseSpot(new Vector(6, 9, 24), -65f),
                new Leaderboard(new Vector(25.5, 9, 35.5)));
    }
}

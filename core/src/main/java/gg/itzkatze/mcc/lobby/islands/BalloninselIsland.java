package gg.itzkatze.mcc.lobby.islands;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.lobby.LobbyFeature;
import gg.itzkatze.mcc.lobby.LobbyIsland;
import gg.itzkatze.mcc.lobby.cosmetics.Leaderboard;
import gg.itzkatze.mcc.lobby.cosmetics.cases.CaseSpot;
import gg.itzkatze.mcc.lobby.jumpandrun.JumpAndRun;
import gg.itzkatze.mcc.lobby.npc.NpcSpot;
import gg.itzkatze.mcc.lobby.npc.impl.CosmeticShopNpc;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BalloninselIsland extends LobbyIsland {

    @Override
    public @NotNull String getId() {
        return "balloninsel";
    }

    @Override
    public @NotNull String getStructureName() {
        return "lobby/balloninsel";
    }

    @Override
    public @NotNull Vector getSpawnPosition() {
        return new Vector(27, 15, 23);
    }

    @Override
    public @NotNull StructureRotation getRotation() {
        return StructureRotation.COUNTERCLOCKWISE_90;
    }

    @Override
    public @NotNull List<LobbyFeature> createFeatures() {
        return List.of(
                new JumpAndRun(new Vector(31, 15, 23), MinecraftChallenges.get()),
                new NpcSpot(new CosmeticShopNpc(), new Vector(26, 16, 18), -45f),
                new CaseSpot(new Vector(24, 16, 19), -45f),
                new Leaderboard(new Vector(29.5, 15, 26.5)));
    }
}

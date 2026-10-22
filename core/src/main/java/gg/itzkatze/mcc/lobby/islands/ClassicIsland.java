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


public class ClassicIsland extends LobbyIsland {

    @Override
    public @NotNull String getId() {
        return "classic";
    }

    @Override
    public @NotNull String getStructureName() {
        return "lobby/classic";
    }

    @Override
    public @NotNull Vector getSpawnPosition() {
        return new Vector(41, 10, 28);
    }

    @Override
    public @NotNull StructureRotation getRotation() {
        return StructureRotation.COUNTERCLOCKWISE_90;
    }

    @Override
    public @NotNull List<LobbyFeature> createFeatures() {
        return List.of(
                new JumpAndRun(new Vector(33, 22, 27), MinecraftChallenges.get()),
                new NpcSpot(new CosmeticShopNpc(), new Vector(21.5, 5, 42.5), 30f),
                new CaseSpot(new Vector(19, 5, 43), 30f),
                new Leaderboard(new Vector(32.5, 23, 31.5)));
    }
}

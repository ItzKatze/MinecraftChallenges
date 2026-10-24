package gg.itzkatze.katze.lobby;

import org.bukkit.block.structure.StructureRotation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class LobbyIsland {

    public abstract @NotNull String getId();

    public abstract @NotNull String getStructureName();

    public abstract @NotNull Vector getSpawnPosition();

    public @NotNull StructureRotation getRotation() {
        return StructureRotation.NONE;
    }

    public float getSpawnYaw() {
        return 0;
    }

    public @NotNull List<LobbyFeature> createFeatures() {
        return List.of();
    }
}

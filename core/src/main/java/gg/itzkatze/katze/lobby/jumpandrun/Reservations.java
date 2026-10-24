package gg.itzkatze.katze.lobby.jumpandrun;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class Reservations {

    private final Map<UUID, Set<Cell>> byOwner = new HashMap<>();
    private final Map<Cell, Set<UUID>> byCell = new HashMap<>();

    void set(@NotNull UUID owner, @NotNull Set<Cell> cells) {
        release(owner);
        byOwner.put(owner, Set.copyOf(cells));
        for (Cell cell : cells) {
            byCell.computeIfAbsent(cell, key -> new HashSet<>()).add(owner);
        }
    }

    void release(@NotNull UUID owner) {
        Set<Cell> cells = byOwner.remove(owner);
        if (cells == null) return;

        for (Cell cell : cells) {
            Set<UUID> owners = byCell.get(cell);
            owners.remove(owner);
            if (owners.isEmpty()) {
                byCell.remove(cell);
            }
        }
    }

    boolean isReservedByOther(@NotNull Cell cell, @NotNull UUID self) {
        Set<UUID> owners = byCell.get(cell);
        if (owners == null) return false;

        for (UUID owner : owners) {
            if (!owner.equals(self)) return true;
        }
        return false;
    }

    void clear() {
        byOwner.clear();
        byCell.clear();
    }
}

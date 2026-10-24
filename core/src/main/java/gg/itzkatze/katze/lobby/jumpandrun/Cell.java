package gg.itzkatze.katze.lobby.jumpandrun;

import org.jetbrains.annotations.NotNull;

record Cell(int x, int y, int z) {

    @NotNull Cell add(int dx, int dy, int dz) {
        return new Cell(x + dx, y + dy, z + dz);
    }
}

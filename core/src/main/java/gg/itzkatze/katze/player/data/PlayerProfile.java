package gg.itzkatze.katze.player.data;

import gg.itzkatze.katze.lobby.cosmetics.CosmeticProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerProfile {

    private @Nullable String name;
    private int xp;
    private CosmeticProfile cosmetics = new CosmeticProfile();

    public @Nullable String name() {
        return name;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public @NotNull CosmeticProfile cosmetics() {
        return cosmetics;
    }

    void rename(@Nullable String name) {
        if (name != null) {
            this.name = name;
        }
    }

    void normalize() {
        xp = Math.max(xp, 0);
        if (cosmetics == null) {
            cosmetics = new CosmeticProfile();
        }
    }
}

package gg.itzkatze.mcc.deathswap;

public enum DeathSwapXp {

    SWAP_SURVIVED(10),
    WIN(150);

    private final int xp;

    DeathSwapXp(int xp) {
        this.xp = xp;
    }

    public int xp() {
        return xp;
    }
}

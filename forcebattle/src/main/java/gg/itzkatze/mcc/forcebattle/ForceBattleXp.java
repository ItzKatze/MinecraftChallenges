package gg.itzkatze.mcc.forcebattle;

public enum ForceBattleXp {

    OBJECTIVE(10),
    WALKING(1),
    WIN(150);

    private final int xp;

    ForceBattleXp(int xp) {
        this.xp = xp;
    }

    public int xp() {
        return xp;
    }
}

package gg.itzkatze.mcc.allitems;

public enum AllItemsXp {

    ITEM(2),
    BIOME(5),
    MOB(5),
    ADVANCEMENT(5),
    STRUCTURE(10);

    private final int xp;

    AllItemsXp(int xp) {
        this.xp = xp;
    }

    public int xp() {
        return xp;
    }
}

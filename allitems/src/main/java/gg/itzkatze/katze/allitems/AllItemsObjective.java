package gg.itzkatze.katze.allitems;

public record AllItemsObjective(AllItemsObjectiveType type, String key, String displayName) {

    public boolean matches(AllItemsObjectiveType otherType, String otherKey) {
        return type == otherType && key.equals(otherKey);
    }
}

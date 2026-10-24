package gg.itzkatze.katze.lobby.cosmetics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CosmeticCatalog {

    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static @Nullable CosmeticCatalog standard;

    private final Map<String, Cosmetic> byId = new LinkedHashMap<>();
    private final Map<CosmeticCategory, List<Cosmetic>> byCategory = new EnumMap<>(CosmeticCategory.class);
    private final List<Cosmetic> all;

    private CosmeticCatalog(@NotNull List<? extends Cosmetic> cosmetics) {
        for (CosmeticCategory category : CosmeticCategory.values()) {
            byCategory.put(category, new ArrayList<>());
        }
        for (Cosmetic cosmetic : cosmetics) {
            String id = cosmetic.id();
            if (!ID.matcher(id).matches()) {
                throw new IllegalStateException("Cosmetic id \"" + id + "\" must only contain lowercase letters, digits and underscores");
            }
            if (cosmetic.rarity() == null) {
                throw new IllegalStateException("Cosmetic \"" + id + "\" has no rarity");
            }
            if (byId.putIfAbsent(id, cosmetic) != null) {
                throw new IllegalStateException("Duplicate cosmetic id \"" + id + "\"");
            }
            byCategory.get(cosmetic.category()).add(cosmetic);
        }
        byCategory.replaceAll((category, list) -> List.copyOf(list));
        this.all = List.copyOf(byId.values());
    }

    public static synchronized @NotNull CosmeticCatalog standard() {
        if (standard == null) {
            List<Cosmetic> cosmetics = new ArrayList<>();
            Collections.addAll(cosmetics, BlockCosmetic.values());
            Collections.addAll(cosmetics, SoundCosmetic.values());
            Collections.addAll(cosmetics, MusicCosmetic.values());
            standard = new CosmeticCatalog(cosmetics);
        }
        return standard;
    }

    public static @NotNull CosmeticCatalog of(@NotNull List<? extends Cosmetic> cosmetics) {
        return new CosmeticCatalog(cosmetics);
    }

    public @NotNull Optional<Cosmetic> find(@NotNull String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public @NotNull List<Cosmetic> in(@NotNull CosmeticCategory category) {
        return byCategory.get(category);
    }

    public @NotNull List<Cosmetic> all() {
        return all;
    }
}

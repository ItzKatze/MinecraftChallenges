package gg.itzkatze.katze.forcebattle.exclusion;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.DamageTypeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.KatzeDamageType;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.MobsSimplified;
import gg.itzkatze.katze.data.StructureSimplified;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ObjectiveFilter {

    private static final List<String> CONFIG_LISTS = List.of("items", "mobs", "biomes", "structures", "advancements", "heights", "deaths");

    private final List<ExclusionRules> activeRules;
    private final Map<String, Set<String>> configEntries;

    private ObjectiveFilter(@NotNull List<ExclusionRules> activeRules, @NotNull Map<String, Set<String>> configEntries) {
        this.activeRules = activeRules;
        this.configEntries = configEntries;
    }

    public static @NotNull ObjectiveFilter current() {
        List<ObjectiveExclusion> active = Arrays.stream(ObjectiveExclusion.values()).filter(ObjectiveExclusion::isActive).toList();
        return of(active, KatzeGames.get().getConfig().getConfigurationSection("exclude"));
    }

    public static @NotNull ObjectiveFilter of(@NotNull Collection<ObjectiveExclusion> activeGroups, @Nullable ConfigurationSection exclude) {
        Map<String, Set<String>> entries = new HashMap<>();
        for (String list : CONFIG_LISTS) {
            List<String> raw = exclude == null ? List.of() : exclude.getStringList(list);
            entries.put(list, raw.stream().map(ObjectiveFilter::normalize).collect(Collectors.toSet()));
        }
        return new ObjectiveFilter(activeGroups.stream().map(ObjectiveExclusion::getRules).toList(), entries);
    }

    public boolean isExcluded(@NotNull Object value) {
        return switch (value) {
            case Material material -> listed("items", material.name()) || anyRule(rules -> rules.excludesItem(material));
            case EntityType type -> listed("mobs", type.name()) || anyRule(rules -> rules.excludesMob(type));
            case Biome biome -> listed("biomes", biome.getKey().getKey()) || anyRule(rules -> rules.excludesBiome(biome.getKey().getKey()));
            case KatzeStructure structure -> listed("structures", structure.name(), structure.getKey().getKey()) || anyRule(rules -> rules.excludesStructure(structure));
            case KatzeAdvancement advancement -> listed("advancements", advancement.name(), advancement.getKey().getKey())
                    || anyRule(rules -> rules.excludesAdvancement(advancement));
            case Integer height -> listed("heights", String.valueOf(height));
            case KatzeDamageType death -> listed("deaths", death.name()) || anyRule(rules -> rules.excludesDeath(death));
            case MobsSimplified simplified -> listed("mobs", simplified.name()) || simplified.getMobs().stream().allMatch(this::isExcluded);
            case BiomeSimplified simplified -> listed("biomes", simplified.name()) || simplified.getBiomes().stream().allMatch(this::isExcluded);
            case StructureSimplified simplified -> listed("structures", simplified.name()) || simplified.getStructures().stream().allMatch(this::isExcluded);
            case DamageTypeSimplified simplified -> listed("deaths", simplified.name()) || simplified.getDamageTypes().stream().allMatch(this::isExcluded);
            default -> false;
        };
    }

    private boolean listed(@NotNull String list, @NotNull String... names) {
        Set<String> entries = configEntries.get(list);
        return Arrays.stream(names).anyMatch(name -> entries.contains(name.toUpperCase(Locale.ROOT)));
    }

    private boolean anyRule(@NotNull Predicate<ExclusionRules> test) {
        return activeRules.stream().anyMatch(test);
    }

    private static @NotNull String normalize(@NotNull String entry) {
        String trimmed = entry.trim();
        return trimmed.substring(trimmed.lastIndexOf(':') + 1).toUpperCase(Locale.ROOT);
    }
}

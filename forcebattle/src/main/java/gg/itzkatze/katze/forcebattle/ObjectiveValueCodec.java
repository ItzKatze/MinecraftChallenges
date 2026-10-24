package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.DamageTypeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.KatzeDamageType;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.data.MobsSimplified;
import gg.itzkatze.katze.data.StructureSimplified;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Converts ForceBattle objective values to and from a kind plus a raw string, so they can be stored as JSON.
 */
final class ObjectiveValueCodec {

    enum Kind {
        MATERIAL,
        ENTITY_TYPE,
        MOBS_SIMPLIFIED,
        BIOME,
        BIOME_SIMPLIFIED,
        ADVANCEMENT,
        HEIGHT,
        LOCATION,
        STRUCTURE,
        STRUCTURE_SIMPLIFIED,
        DAMAGE_TYPE,
        DAMAGE_TYPE_SIMPLIFIED
    }

    static final class EncodedValue {
        String kind;
        String raw;

        private EncodedValue() {
        }

        EncodedValue(@NotNull Kind kind, @NotNull String raw) {
            this.kind = kind.name();
            this.raw = raw;
        }
    }

    private ObjectiveValueCodec() {
    }

    static @Nullable EncodedValue encode(@Nullable Object value) {
        return switch (value) {
            case Material material -> new EncodedValue(Kind.MATERIAL, material.name());
            case EntityType entityType -> new EncodedValue(Kind.ENTITY_TYPE, entityType.name());
            case MobsSimplified mobs -> new EncodedValue(Kind.MOBS_SIMPLIFIED, mobs.name());
            case Biome biome -> new EncodedValue(Kind.BIOME, biome.getKey().toString());
            case BiomeSimplified biome -> new EncodedValue(Kind.BIOME_SIMPLIFIED, biome.name());
            case KatzeAdvancement advancement -> new EncodedValue(Kind.ADVANCEMENT, advancement.name());
            case Integer height -> new EncodedValue(Kind.HEIGHT, height.toString());
            case Location location when location.isWorldLoaded() -> new EncodedValue(Kind.LOCATION, String.join(",",
                    location.getWorld().getName(),
                    Integer.toString(location.getBlockX()),
                    Integer.toString(location.getBlockY()),
                    Integer.toString(location.getBlockZ())));
            case KatzeStructure structure -> new EncodedValue(Kind.STRUCTURE, structure.name());
            case StructureSimplified structure -> new EncodedValue(Kind.STRUCTURE_SIMPLIFIED, structure.name());
            case KatzeDamageType damageType -> new EncodedValue(Kind.DAMAGE_TYPE, damageType.name());
            case DamageTypeSimplified damageType -> new EncodedValue(Kind.DAMAGE_TYPE_SIMPLIFIED, damageType.name());
            case null, default -> null;
        };
    }

    static @Nullable Object decode(@Nullable EncodedValue encoded) {
        if (encoded == null || encoded.kind == null || encoded.raw == null) {
            return null;
        }
        String raw = encoded.raw;
        try {
            return switch (Kind.valueOf(encoded.kind)) {
                case MATERIAL -> Material.getMaterial(raw);
                case ENTITY_TYPE -> EntityType.valueOf(raw);
                case MOBS_SIMPLIFIED -> MobsSimplified.valueOf(raw);
                case BIOME -> decodeBiome(raw);
                case BIOME_SIMPLIFIED -> BiomeSimplified.valueOf(raw);
                case ADVANCEMENT -> KatzeAdvancement.valueOf(raw);
                case HEIGHT -> Integer.valueOf(raw);
                case LOCATION -> decodeLocation(raw);
                case STRUCTURE -> KatzeStructure.valueOf(raw);
                case STRUCTURE_SIMPLIFIED -> StructureSimplified.valueOf(raw);
                case DAMAGE_TYPE -> KatzeDamageType.valueOf(raw);
                case DAMAGE_TYPE_SIMPLIFIED -> DamageTypeSimplified.valueOf(raw);
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable Biome decodeBiome(@NotNull String raw) {
        NamespacedKey key = NamespacedKey.fromString(raw);
        return key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
    }

    private static @Nullable Location decodeLocation(@NotNull String raw) {
        String[] parts = raw.split(",");
        if (parts.length < 4) {
            return null;
        }
        int coordinates = parts.length - 3;
        World world = Bukkit.getWorld(String.join(",", Arrays.copyOfRange(parts, 0, coordinates)));
        if (world == null) {
            return null;
        }
        return new Location(world,
                Integer.parseInt(parts[coordinates]),
                Integer.parseInt(parts[coordinates + 1]),
                Integer.parseInt(parts[coordinates + 2]));
    }
}

package gg.itzkatze.katze.data;

import lombok.Getter;
import org.bukkit.damage.DamageType;

import java.util.List;

@Getter
public enum KatzeDamageType {
    ARROW("Arrow", List.of(DamageType.ARROW)),
    CACTUS("Cactus", List.of(DamageType.CACTUS)),
    CAMPFIRE("Campfire", List.of(DamageType.CAMPFIRE)),
    CRAMMING("Cramming", List.of(DamageType.CRAMMING)),
    DRAGON_BREATH("Dragon Breath", List.of(DamageType.DRAGON_BREATH)),
    DROWN("Drown", List.of(DamageType.DROWN)),
    ENDER_PEARL("Ender Pearl", List.of(DamageType.ENDER_PEARL)),
    EXPLOSION("Explosion", List.of(DamageType.EXPLOSION)),
    FALL("Fall", List.of(DamageType.FALL)),
    FALLING_ANVIL("Falling Anvil", List.of(DamageType.FALLING_ANVIL)),
    FALLING_BLOCK("Falling Block", List.of(DamageType.FALLING_BLOCK)),
    FALLING_STALACTITE("Falling Stalactite", List.of(DamageType.FALLING_STALACTITE)),
    FIREBALL("Fireball", List.of(DamageType.FIREBALL, DamageType.UNATTRIBUTED_FIREBALL)),
    FIREWORKS("Fireworks", List.of(DamageType.FIREWORKS)),
    FLY_INTO_WALL("Fly Into Wall", List.of(DamageType.FLY_INTO_WALL)),
    FREEZE("Freeze", List.of(DamageType.FREEZE)),
    HOT_FLOOR("Hot Floor", List.of(DamageType.HOT_FLOOR)),
    IN_FIRE("In Fire", List.of(DamageType.IN_FIRE)),
    IN_WALL("In Wall", List.of(DamageType.IN_WALL)),
    LAVA("Lava", List.of(DamageType.LAVA)),
    LIGHTNING_BOLT("Lightning Bolt", List.of(DamageType.LIGHTNING_BOLT)),
    MAGIC("Magic", List.of(DamageType.MAGIC)),
    MOB_ATTACK("Mob Attack", List.of(DamageType.MOB_ATTACK)),
    MOB_PROJECTILE("Mob Projectile", List.of(DamageType.MOB_PROJECTILE)),
    ON_FIRE("On Fire", List.of(DamageType.ON_FIRE)),
    OUT_OF_WORLD("Out of World", List.of(DamageType.OUT_OF_WORLD)),
    PLAYER_EXPLOSION("Player Explosion", List.of(DamageType.PLAYER_EXPLOSION)),
    SONIC_BOOM("Sonic Boom", List.of(DamageType.SONIC_BOOM)),
    SPIT("Spit", List.of(DamageType.SPIT)),
    STALAGMITE("Stalagmite", List.of(DamageType.STALAGMITE)),
    STING("Sting", List.of(DamageType.STING)),
    SULFUR_CUBE_HOT("Hot Sulfur Cube", List.of(DamageType.SULFUR_CUBE_HOT)),
    SWEET_BERRY_BUSH("Sweet Berry Bush", List.of(DamageType.SWEET_BERRY_BUSH)),
    TRIDENT("Trident", List.of(DamageType.TRIDENT)),
    WIND_CHARGE("Wind Charge", List.of(DamageType.WIND_CHARGE)),
    WITHER("Wither", List.of(DamageType.WITHER, DamageType.WITHER_SKULL)),
    ;

    private final String name;
    private final List<DamageType> types;

    KatzeDamageType(String name, List<DamageType> types) {
        this.name = name;
        this.types = types;
    }
}

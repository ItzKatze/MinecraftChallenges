package gg.itzkatze.katze.data;

import gg.itzkatze.katze.gui.GUIMaterial;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public enum DamageTypeSimplified {
    CONTACT("Contact", new GUIMaterial(Material.CACTUS), List.of(KatzeDamageType.CACTUS, KatzeDamageType.CAMPFIRE, KatzeDamageType.STALAGMITE)),
    ENTITY_ATTACK("Entity Attack", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(KatzeDamageType.MOB_ATTACK, KatzeDamageType.MOB_PROJECTILE, KatzeDamageType.SONIC_BOOM,
            KatzeDamageType.SPIT, KatzeDamageType.STING, KatzeDamageType.SULFUR_CUBE_HOT, KatzeDamageType.TRIDENT, KatzeDamageType.WITHER, KatzeDamageType.WIND_CHARGE)),
    PROJECTILE("Projectile", new GUIMaterial(Material.ARROW), List.of(KatzeDamageType.ARROW, KatzeDamageType.MOB_PROJECTILE, KatzeDamageType.TRIDENT,
            KatzeDamageType.WIND_CHARGE, KatzeDamageType.DRAGON_BREATH, KatzeDamageType.FIREBALL)),
    SUFFOCATION("Suffocation", new GUIMaterial(Material.GRAVEL), List.of(KatzeDamageType.CRAMMING, KatzeDamageType.IN_WALL)),
    FALL("Fall", new GUIMaterial(Material.FEATHER), List.of(KatzeDamageType.FALL)),
    FIRE("Fire", new GUIMaterial(Material.FLINT_AND_STEEL), List.of(KatzeDamageType.IN_FIRE, KatzeDamageType.ON_FIRE)),
    LAVA("Lava", new GUIMaterial(Material.LAVA_BUCKET), List.of(KatzeDamageType.LAVA)),
    DROWNING("Drowning", new GUIMaterial(Material.WATER_BUCKET), List.of(KatzeDamageType.DROWN)),
    EXPLOSION("Explosion", new GUIMaterial(Material.TNT), List.of(KatzeDamageType.EXPLOSION, KatzeDamageType.PLAYER_EXPLOSION, KatzeDamageType.FIREWORKS,
            KatzeDamageType.FIREBALL)),
    FALLING_BLOCK("Falling Block", new GUIMaterial(Material.POINTED_DRIPSTONE), List.of(KatzeDamageType.FALLING_ANVIL, KatzeDamageType.FALLING_BLOCK,
            KatzeDamageType.FALLING_STALACTITE)),
    HOT_FLOOR("Hot Floor", new GUIMaterial(Material.MAGMA_BLOCK), List.of(KatzeDamageType.HOT_FLOOR)),
    KINETIC_ENERGY("Kinetic Energy", new GUIMaterial(Material.ELYTRA), List.of(KatzeDamageType.FLY_INTO_WALL, KatzeDamageType.ENDER_PEARL)),
    NATURAL("Natural", new GUIMaterial(Material.POWDER_SNOW_BUCKET), List.of(KatzeDamageType.FREEZE, KatzeDamageType.LIGHTNING_BOLT, KatzeDamageType.SWEET_BERRY_BUSH)),
    ;

    private final String name;
    private final GUIMaterial guiMaterial;
    private final List<KatzeDamageType> damageTypes;

    DamageTypeSimplified(String name, GUIMaterial guiMaterial, List<KatzeDamageType> damageTypes) {
        this.name = name;
        this.guiMaterial = guiMaterial;
        this.damageTypes = damageTypes;
    }
}

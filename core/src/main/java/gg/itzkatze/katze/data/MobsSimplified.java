package gg.itzkatze.katze.data;

import gg.itzkatze.katze.gui.GUIMaterial;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

@Getter
public enum MobsSimplified {
    AIRBORNE("Airborne", new GUIMaterial(Material.PHANTOM_MEMBRANE), List.of(EntityType.ALLAY, EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.ENDER_DRAGON, EntityType.GHAST,
            EntityType.HAPPY_GHAST, EntityType.PARROT, EntityType.PHANTOM, EntityType.VEX)),
    ANIMAL("Animal", new GUIMaterial(Material.WHEAT), List.of(EntityType.ARMADILLO, EntityType.CAMEL, EntityType.CAT, EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG, EntityType.GOAT, EntityType.HORSE, EntityType.LLAMA, EntityType.MOOSHROOM, EntityType.MULE, EntityType.OCELOT, EntityType.PANDA,
            EntityType.PIG, EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.SHEEP, EntityType.SNIFFER, EntityType.TRADER_LLAMA, EntityType.WOLF)),
    AQUATIC("Aquatic", new GUIMaterial(Material.TRIDENT), List.of(EntityType.AXOLOTL, EntityType.COD, EntityType.DOLPHIN, EntityType.GLOW_SQUID, EntityType.NAUTILUS,
            EntityType.PUFFERFISH,
            EntityType.SALMON, EntityType.SQUID, EntityType.TADPOLE, EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.ZOMBIE_NAUTILUS, EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN, EntityType.DROWNED)),
    ARTHROPOD("Arthropod", new GUIMaterial(Material.STRING), List.of(EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.SPIDER)),
    CONSTRUCT("Construct", new GUIMaterial(Material.IRON_BLOCK), List.of(EntityType.COPPER_GOLEM, EntityType.CREAKING, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM)),
    CUBIC("Cubic", new GUIMaterial(Material.SLIME_BLOCK), List.of(EntityType.MAGMA_CUBE, EntityType.SLIME, EntityType.SULFUR_CUBE)),
    ENDER("Ender", new GUIMaterial(Material.END_STONE), List.of(EntityType.ENDERMAN, EntityType.SHULKER, EntityType.ENDER_DRAGON, EntityType.ENDERMITE)),
    FROZEN("Frozen", new GUIMaterial(Material.ICE), List.of(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM)),
    HUMANOID("Humanoid", new GUIMaterial(Material.PLAYER_HEAD), List.of(EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.PILLAGER, EntityType.VILLAGER, EntityType.VINDICATOR,
            EntityType.WANDERING_TRADER, EntityType.WITCH)),
    INFERNAL("Infernal", new GUIMaterial(Material.BLAZE_ROD), List.of(EntityType.HOGLIN, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.STRIDER,
            EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.BLAZE, EntityType.GHAST)),
    MYTHICAL("Mythical", new GUIMaterial(Material.BREEZE_ROD), List.of(EntityType.BREEZE, EntityType.RAVAGER, EntityType.WARDEN, EntityType.WITHER, EntityType.GIANT)),
    SKELETAL("Skeletal", new GUIMaterial(Material.SKELETON_SKULL), List.of(EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.WITHER_SKELETON, EntityType.BOGGED, EntityType.STRAY,
            EntityType.PARCHED)),
    UNDEAD("Undead", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(EntityType.CAMEL_HUSK, EntityType.DROWNED, EntityType.HUSK, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.GIANT)),
    EXPLOSIVE("Explosive", new GUIMaterial(Material.TNT), List.of(EntityType.CREEPER, EntityType.WITHER));

    private final String name;
    private final GUIMaterial guiMaterial;
    private final List<EntityType> mobs;

    MobsSimplified(String name, GUIMaterial guiMaterial, List<EntityType> mobs) {
        this.name = name;
        this.guiMaterial = guiMaterial;
        this.mobs = mobs;
    }
}

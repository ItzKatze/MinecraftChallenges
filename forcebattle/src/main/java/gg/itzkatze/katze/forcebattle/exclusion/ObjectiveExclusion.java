package gg.itzkatze.katze.forcebattle.exclusion;

import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.KatzeDamageType;
import gg.itzkatze.katze.data.KatzeStructure;
import gg.itzkatze.katze.forcebattle.ForceBattleSettings;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
public enum ObjectiveExclusion {
    UNOBTAINABLE(null, Material.BARRIER, "Objectives that can't be obtained in survival", ExclusionRules.builder()
            .items(Material.AIR, Material.BEDROCK, Material.BARRIER, Material.LIGHT, Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID, Material.JIGSAW,
                    Material.TEST_BLOCK, Material.TEST_INSTANCE_BLOCK, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
                    Material.COMMAND_BLOCK_MINECART, Material.DEBUG_STICK, Material.KNOWLEDGE_BOOK, Material.SPAWNER, Material.TRIAL_SPAWNER, Material.VAULT,
                    Material.BUDDING_AMETHYST, Material.REINFORCED_DEEPSLATE, Material.END_PORTAL_FRAME, Material.CHORUS_PLANT, Material.FARMLAND, Material.DIRT_PATH,
                    Material.FROGSPAWN, Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL, Material.PLAYER_HEAD, Material.PETRIFIED_OAK_SLAB, Material.POTION,
                    Material.TIPPED_ARROW, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.WRITTEN_BOOK)
            .itemsNamed(name -> name.startsWith("INFESTED_") || name.endsWith("_SPAWN_EGG"))
            .mobs(EntityType.GIANT, EntityType.ILLUSIONER, EntityType.MANNEQUIN, EntityType.ARMOR_STAND, EntityType.CREAKING)
            .biomes("the_void")),
    MUSIC_DISCS(ForceBattleSettings.EXCLUDE_MUSIC_DISCS, Material.MUSIC_DISC_CAT, "Music discs and the disc fragment", ExclusionRules.builder()
            .items(Material.DISC_FRAGMENT_5)
            .itemsNamed(name -> name.startsWith("MUSIC_DISC_"))),
    BANNER_PATTERNS(ForceBattleSettings.EXCLUDE_BANNER_PATTERNS, Material.GLOBE_BANNER_PATTERN,
            "Banner patterns except Field Masoned, Bordure Indented and Flower Charge",
            ExclusionRules.builder().itemsNamed(ObjectiveExclusion::isRareBannerPattern)),
    ARMOR_TEMPLATES(ForceBattleSettings.EXCLUDE_ARMOR_TEMPLATES, Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, "Armor trim and netherite upgrade templates",
            ExclusionRules.builder().itemsNamed(name -> name.endsWith("_SMITHING_TEMPLATE"))),
    POTTERY_SHERDS(ForceBattleSettings.EXCLUDE_POTTERY_SHERDS, Material.ANGLER_POTTERY_SHERD, "All pottery sherds", ExclusionRules.builder()
            .itemsNamed(name -> name.endsWith("_POTTERY_SHERD"))),
    ORES(ForceBattleSettings.EXCLUDE_ORES, Material.DIAMOND_ORE, "All ore blocks", ExclusionRules.builder()
            .itemsNamed(name -> name.endsWith("_ORE"))),
    END(ForceBattleSettings.EXCLUDE_END, Material.END_STONE, "End items, mobs, biomes, structures, advancements and deaths", ExclusionRules.builder()
            .items(Material.END_STONE, Material.END_STONE_BRICKS, Material.END_STONE_BRICK_SLAB, Material.END_STONE_BRICK_STAIRS, Material.END_STONE_BRICK_WALL,
                    Material.END_ROD, Material.ELYTRA, Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.PURPUR_SLAB, Material.PURPUR_STAIRS,
                    Material.CHORUS_FLOWER, Material.CHORUS_FRUIT, Material.POPPED_CHORUS_FRUIT, Material.DRAGON_BREATH, Material.DRAGON_EGG, Material.DRAGON_HEAD,
                    Material.SHULKER_SHELL, Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
            .itemsNamed(name -> name.endsWith("SHULKER_BOX"))
            .mobs(EntityType.ENDER_DRAGON, EntityType.SHULKER)
            .biomes("the_end", "end_barrens", "end_highlands", "end_midlands", "small_end_islands")
            .structures(KatzeStructure.END_CITY)
            .advancements(KatzeAdvancement.THE_END, KatzeAdvancement.FREE_THE_END, KatzeAdvancement.REMOTE_GATEWAY, KatzeAdvancement.THE_END_AGAIN,
                    KatzeAdvancement.YOU_NEED_A_MINT, KatzeAdvancement.THE_CITY_AT_THE_END_OF_THE_GAME, KatzeAdvancement.SKY_IS_THE_LIMIT,
                    KatzeAdvancement.GREAT_VIEW_FROM_UP_HERE, KatzeAdvancement.IS_IT_A_PLANE)
            .deaths(KatzeDamageType.DRAGON_BREATH, KatzeDamageType.FLY_INTO_WALL)),
    TRIAL_CHAMBERS(ForceBattleSettings.EXCLUDE_TRIAL_ITEMS, Material.TRIAL_SPAWNER, "Trial chamber loot, the Breeze and wind charge deaths", ExclusionRules.builder()
            .items(Material.TRIAL_KEY, Material.OMINOUS_TRIAL_KEY, Material.HEAVY_CORE, Material.MACE, Material.BREEZE_ROD, Material.WIND_CHARGE,
                    Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, Material.FLOW_BANNER_PATTERN,
                    Material.GUSTER_BANNER_PATTERN, Material.FLOW_POTTERY_SHERD, Material.GUSTER_POTTERY_SHERD, Material.SCRAPE_POTTERY_SHERD,
                    Material.MUSIC_DISC_PRECIPICE, Material.MUSIC_DISC_CREATOR, Material.MUSIC_DISC_CREATOR_MUSIC_BOX)
            .mobs(EntityType.BREEZE)
            .deaths(KatzeDamageType.WIND_CHARGE)),
    OXIDIZED_COPPER(ForceBattleSettings.EXCLUDE_OXIDIZED_COPPER, Material.OXIDIZED_COPPER_LANTERN, "Copper items that only exist through oxidation",
            ExclusionRules.builder().itemsNamed(ObjectiveExclusion::isOxidationOnlyCopper)),
    HEADS(ForceBattleSettings.EXCLUDE_HEADS, Material.PIGLIN_HEAD, "Mob heads, skulls and the banner patterns made from them", ExclusionRules.builder()
            .items(Material.CREEPER_HEAD, Material.ZOMBIE_HEAD, Material.PIGLIN_HEAD, Material.DRAGON_HEAD, Material.SKELETON_SKULL, Material.WITHER_SKELETON_SKULL,
                    Material.CREEPER_BANNER_PATTERN, Material.SKULL_BANNER_PATTERN)
            .advancements(KatzeAdvancement.SPOOKY_SCARY_SKELETON)),
    SNIFFER(ForceBattleSettings.EXCLUDE_SNIFFER, Material.SNIFFER_EGG, "Sniffer items, the Sniffer and its advancements", ExclusionRules.builder()
            .items(Material.SNIFFER_EGG, Material.TORCHFLOWER_SEEDS, Material.TORCHFLOWER, Material.PITCHER_POD, Material.PITCHER_PLANT)
            .mobs(EntityType.SNIFFER)
            .advancements(KatzeAdvancement.SMELLS_INTERESTING, KatzeAdvancement.LITTLE_SNIFFS, KatzeAdvancement.PLANTING_THE_PAST)),
    UNREALISTIC(ForceBattleSettings.EXCLUDE_UNREALISTIC, Material.CLOCK, "Very long advancements and thunderstorm-only objectives", ExclusionRules.builder()
            .advancements(KatzeAdvancement.HOW_DID_WE_GET_HERE, KatzeAdvancement.ADVENTURING_TIME, KatzeAdvancement.A_BALANCED_DIET, KatzeAdvancement.MONSTERS_HUNTED,
                    KatzeAdvancement.TWO_BY_TWO, KatzeAdvancement.A_COMPLETE_CATALOGUE, KatzeAdvancement.SMITHING_WITH_STYLE, KatzeAdvancement.A_FURIOUS_COCKTAIL,
                    KatzeAdvancement.BEACONATOR, KatzeAdvancement.WITH_OUR_POWERS_COMBINED, KatzeAdvancement.WHEN_THE_SQUAD_HOPS_INTO_TOWN,
                    KatzeAdvancement.COVER_ME_IN_DEBRIS, KatzeAdvancement.VERY_VERY_FRIGHTENING, KatzeAdvancement.SURGE_PROTECTOR)
            .mobs(EntityType.SKELETON_HORSE)
            .deaths(KatzeDamageType.LIGHTNING_BOLT)
            .items(Material.MACE, Material.HEAVY_CORE, Material.BEACON, Material.NETHER_STAR, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
                    Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.OCHRE_FROGLIGHT, Material.PEARLESCENT_FROGLIGHT, Material.VERDANT_FROGLIGHT,
                    Material.OMINOUS_TRIAL_KEY, Material.ECHO_SHARD)),
    MAPS(ForceBattleSettings.EXCLUDE_MAPS, Material.FILLED_MAP, "Structure maps from cartographer trades and abandoned camp chests",
            ExclusionRules.builder().itemsNamed(ObjectiveExclusion::isStructureMap));

    private static final Set<String> COMMON_BANNER_PATTERNS = Set.of("FIELD_MASONED_BANNER_PATTERN", "BORDURE_INDENTED_BANNER_PATTERN", "FLOWER_BANNER_PATTERN");
    private static final List<String> OXIDATION_STAGES = List.of("EXPOSED_", "WEATHERED_", "OXIDIZED_");
    private static final Set<String> CRAFTED_UNOXIDIZED_ONLY = Set.of("COPPER_BARS", "COPPER_CHAIN", "COPPER_LANTERN", "LIGHTNING_ROD");

    private final @Nullable ForceBattleSettings setting;
    private final @NotNull Material icon;
    private final @NotNull String description;
    private final @NotNull ExclusionRules rules;

    ObjectiveExclusion(@Nullable ForceBattleSettings setting, @NotNull Material icon, @NotNull String description, @NotNull ExclusionRules.Builder rules) {
        this.setting = setting;
        this.icon = icon;
        this.description = description;
        this.rules = rules.build();
    }

    public boolean isActive() {
        return setting == null || SettingsManager.isEnabled(setting);
    }

    public static @NotNull List<ObjectiveExclusion> toggleable() {
        return Arrays.stream(values()).filter(exclusion -> exclusion.setting != null).toList();
    }

    private static boolean isRareBannerPattern(@NotNull String name) {
        return name.endsWith("_BANNER_PATTERN") && !COMMON_BANNER_PATTERNS.contains(name);
    }

    /** The filled map is what an empty map turns into when used, so it stays obtainable and is not a structure map. */
    private static boolean isStructureMap(@NotNull String name) {
        return name.endsWith("_MAP") && !name.equals("FILLED_MAP");
    }

    /**
     * Trial chambers only contain unoxidized and oxidized copper blocks, which an axe scrapes back stage by stage. These items can't be scraped from any
     * structure block and are only crafted unoxidized, so their aged variants (and every copper golem statue) need natural oxidation.
     */
    private static boolean isOxidationOnlyCopper(@NotNull String name) {
        String unwaxed = name.startsWith("WAXED_") ? name.substring("WAXED_".length()) : name;
        if (unwaxed.endsWith("COPPER_GOLEM_STATUE")) {
            return true;
        }
        return OXIDATION_STAGES.stream().anyMatch(stage -> unwaxed.startsWith(stage) && CRAFTED_UNOXIDIZED_ONLY.contains(unwaxed.substring(stage.length())));
    }
}

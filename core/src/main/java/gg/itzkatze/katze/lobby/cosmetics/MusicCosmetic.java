package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MusicCosmetic implements Cosmetic {

    DISC_13("disc_13", "music_disc.13", "13", Rarity.UNCOMMON, Material.MUSIC_DISC_13, 179),
    CAT("cat", "music_disc.cat", "Cat", Rarity.RARE, Material.MUSIC_DISC_CAT, 186),
    BLOCKS("blocks", "music_disc.blocks", "Blocks", Rarity.UNCOMMON, Material.MUSIC_DISC_BLOCKS, 346),
    CHIRP("chirp", "music_disc.chirp", "Chirp", Rarity.RARE, Material.MUSIC_DISC_CHIRP, 186),
    FAR("far", "music_disc.far", "Far", Rarity.RARE, Material.MUSIC_DISC_FAR, 175),
    MALL("mall", "music_disc.mall", "Mall", Rarity.UNCOMMON, Material.MUSIC_DISC_MALL, 198),
    MELLOHI("mellohi", "music_disc.mellohi", "Mellohi", Rarity.UNCOMMON, Material.MUSIC_DISC_MELLOHI, 97),
    STAL("stal", "music_disc.stal", "Stal", Rarity.RARE, Material.MUSIC_DISC_STAL, 151),
    STRAD("strad", "music_disc.strad", "Strad", Rarity.RARE, Material.MUSIC_DISC_STRAD, 189),
    DISC_11("disc_11", "music_disc.11", "11", Rarity.EPIC, Material.MUSIC_DISC_11, 72),
    WARD("ward", "music_disc.ward", "Ward", Rarity.RARE, Material.MUSIC_DISC_WARD, 252),
    WAIT("wait", "music_disc.wait", "Wait", Rarity.RARE, Material.MUSIC_DISC_WAIT, 238),
    OTHERSIDE("otherside", "music_disc.otherside", "Otherside", Rarity.EPIC, Material.MUSIC_DISC_OTHERSIDE, 196),
    PIGSTEP("pigstep", "music_disc.pigstep", "Pigstep", Rarity.LEGENDARY, Material.MUSIC_DISC_PIGSTEP, 149),
    CREATOR("creator", "music_disc.creator", "Creator", Rarity.RARE, Material.MUSIC_DISC_CREATOR, 177),
    PRECIPICE("precipice", "music_disc.precipice", "Precipice", Rarity.RARE, Material.MUSIC_DISC_PRECIPICE, 300),
    RELIC("relic", "music_disc.relic", "Relic", Rarity.EPIC, Material.MUSIC_DISC_RELIC, 219),
    DISC_5("disc_5", "music_disc.5", "5", Rarity.EPIC, Material.MUSIC_DISC_5, 179),
    TEARS("tears", "music_disc.tears", "Tears", Rarity.UNCOMMON, Material.MUSIC_DISC_TEARS, 176),
    LAVA_CHICKEN("lava_chicken", "music_disc.lava_chicken", "Lava Chicken", Rarity.LEGENDARY, Material.MUSIC_DISC_LAVA_CHICKEN, 135),
    BOUNCE("bounce", "music_disc.bounce", "Bounce", Rarity.UNCOMMON, Material.MUSIC_DISC_BOUNCE, 235),

    SONG_MINECRAFT("song_minecraft", "Minecraft", Rarity.LEGENDARY, Material.GRASS_BLOCK, "music/game/minecraft"),
    SONG_CLARK("song_clark", "Clark", Rarity.RARE, Material.GRASS_BLOCK, "music/game/clark"),
    SONG_SWEDEN("song_sweden", "Sweden", Rarity.LEGENDARY, Material.GRASS_BLOCK, "music/game/sweden"),
    SONG_SUBWOOFER_LULLABY("song_subwoofer_lullaby", "Subwoofer Lullaby", Rarity.EPIC, Material.GRASS_BLOCK, "music/game/subwoofer_lullaby"),
    SONG_LIVING_MICE("song_living_mice", "Living Mice", Rarity.RARE, Material.GRASS_BLOCK, "music/game/living_mice"),
    SONG_HAGGSTROM("song_haggstrom", "Haggstrom", Rarity.RARE, Material.GRASS_BLOCK, "music/game/haggstrom"),
    SONG_DANNY("song_danny", "Danny", Rarity.RARE, Material.GRASS_BLOCK, "music/game/danny"),
    SONG_KEY("song_key", "Key", Rarity.UNCOMMON, Material.GRASS_BLOCK, "music/game/key"),
    SONG_OXYGENE("song_oxygene", "Oxygène", Rarity.UNCOMMON, Material.GRASS_BLOCK, "music/game/oxygene"),
    SONG_DRY_HANDS("song_dry_hands", "Dry Hands", Rarity.EPIC, Material.GRASS_BLOCK, "music/game/dry_hands"),
    SONG_WET_HANDS("song_wet_hands", "Wet Hands", Rarity.EPIC, Material.GRASS_BLOCK, "music/game/wet_hands"),
    SONG_MICE_ON_VENUS("song_mice_on_venus", "Mice on Venus", Rarity.LEGENDARY, Material.GRASS_BLOCK, "music/game/mice_on_venus"),

    SONG_ARIA_MATH("song_aria_math", "Aria Math", Rarity.LEGENDARY, Material.CRAFTING_TABLE, "music/game/creative/aria_math"),
    SONG_BIOME_FEST("song_biome_fest", "Biome Fest", Rarity.UNCOMMON, Material.CRAFTING_TABLE, "music/game/creative/biome_fest"),
    SONG_BLIND_SPOTS("song_blind_spots", "Blind Spots", Rarity.UNCOMMON, Material.CRAFTING_TABLE, "music/game/creative/blind_spots"),
    SONG_DREITON("song_dreiton", "Dreiton", Rarity.RARE, Material.CRAFTING_TABLE, "music/game/creative/dreiton"),
    SONG_HAUNT_MUSKIE("song_haunt_muskie", "Haunt Muskie", Rarity.RARE, Material.CRAFTING_TABLE, "music/game/creative/haunt_muskie"),
    SONG_TASWELL("song_taswell", "Taswell", Rarity.EPIC, Material.CRAFTING_TABLE, "music/game/creative/taswell"),

    SONG_BALLAD_OF_THE_CATS("song_ballad_of_the_cats", "Ballad of the Cats", Rarity.RARE, Material.NETHERRACK, "music/game/nether/ballad_of_the_cats"),
    SONG_CONCRETE_HALLS("song_concrete_halls", "Concrete Halls", Rarity.RARE, Material.NETHERRACK, "music/game/nether/concrete_halls"),
    SONG_DEAD_VOXEL("song_dead_voxel", "Dead Voxel", Rarity.RARE, Material.NETHERRACK, "music/game/nether/dead_voxel"),
    SONG_WARMTH("song_warmth", "Warmth", Rarity.EPIC, Material.NETHERRACK, "music/game/nether/warmth"),

    SONG_THE_END("song_the_end", "The End", Rarity.LEGENDARY, Material.END_STONE, "music/game/end/the_end"),
    SONG_ALPHA("song_alpha", "Alpha", Rarity.LEGENDARY, Material.END_STONE, "music/game/end/alpha"),
    SONG_BOSS("song_boss", "Boss", Rarity.EPIC, Material.END_STONE, "music/game/end/boss"),

    SONG_BEGINNING_2("song_beginning_2", "Beginning 2", Rarity.UNCOMMON, Material.PAINTING, "music/menu/beginning_2"),
    SONG_FLOATING_TREES("song_floating_trees", "Floating Trees", Rarity.RARE, Material.PAINTING, "music/menu/floating_trees"),
    SONG_MOOG_CITY_2("song_moog_city_2", "Moog City 2", Rarity.RARE, Material.PAINTING, "music/menu/moog_city_2"),
    SONG_MUTATION("song_mutation", "Mutation", Rarity.UNCOMMON, Material.PAINTING, "music/menu/mutation"),

    SONG_AXOLOTL("song_axolotl", "Axolotl", Rarity.UNCOMMON, Material.HEART_OF_THE_SEA, "music/game/water/axolotl"),
    SONG_DRAGON_FISH("song_dragon_fish", "Dragon Fish", Rarity.RARE, Material.HEART_OF_THE_SEA, "music/game/water/dragon_fish"),
    SONG_SHUNIJI("song_shuniji", "Shuniji", Rarity.RARE, Material.HEART_OF_THE_SEA, "music/game/water/shuniji");

    private final String id;
    private final @Nullable Sound sound;
    private final String displayName;
    private final Rarity rarity;
    private final Material icon;
    private final int durationSeconds;
    private final @Nullable String file;

    MusicCosmetic(String id, String key, String displayName, Rarity rarity, Material icon, int durationSeconds) {
        this(id, Sound.sound(Key.key(key), Sound.Source.RECORD, 1f, 1f), displayName, rarity, icon, durationSeconds, null);
    }

    MusicCosmetic(String id, String displayName, Rarity rarity, Material icon, String file) {
        this(id, null, displayName, rarity, icon, 0, file);
    }

    MusicCosmetic(String id, @Nullable Sound sound, String displayName, Rarity rarity, Material icon, int durationSeconds, @Nullable String file) {
        this.id = id;
        this.sound = sound;
        this.displayName = displayName;
        this.rarity = rarity;
        this.icon = icon;
        this.durationSeconds = durationSeconds;
        this.file = file;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public @Nullable String file() {
        return file;
    }

    public boolean isSong() {
        return file != null;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull CosmeticCategory category() {
        return CosmeticCategory.MUSIC;
    }

    @Override
    public @NotNull String displayName() {
        return displayName;
    }

    @Override
    public @NotNull Rarity rarity() {
        return rarity;
    }

    @Override
    public @NotNull Material icon() {
        return icon;
    }

    @Override
    public @Nullable Season season() {
        return null;
    }

    public @Nullable Sound sound() {
        return sound;
    }
}
